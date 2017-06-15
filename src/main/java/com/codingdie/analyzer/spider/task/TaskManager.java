package com.codingdie.analyzer.spider.task;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.util.Timeout;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.storage.spider.TaskStorage;
import com.codingdie.analyzer.util.MailUtil;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.lang.reflect.ParameterizedType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/6/12.
 */
public class TaskManager<T extends Task> {
    private Logger logger = Logger.getLogger("index-task");

    private int totalTaskSize = 0;
    private int lastFinishedTaskSize = 0;
    private long beginTime = 0;
    private int failedCount = 0;
    private boolean failedFlag = false;

    private List<ActorRef> slaves = new Vector<ActorRef>();
    private List<Cancellable> cancellables = new ArrayList<>();

    private ConcurrentHashMap<String, T> todoTasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, T> excutingTasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, T> finishedTasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, T> failedTasks = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Integer> slavesRunningTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFinishedTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFailedTaskMapData = new ConcurrentHashMap<>();
    private TaskStorage<T> taskStorage;
    private ActorSystem actorSystem;
    private ActorRef receiverActor;

    public TaskManager(Class<T> tClass,TieBaFileSystem tieBaFileSystem, ActorSystem actorSystem, String salveActorUri) {
        this.actorSystem = actorSystem;
        this.taskStorage = tieBaFileSystem.getTaskStorage(tClass);
        List<T> list = taskStorage.parseAndRebuild();
        totalTaskSize = list.size();
        list.iterator().forEachRemaining(i -> {
            if (i.status != T.STATUS_FINISHED) {
                todoTasks.put(i.getKey(), i);
            } else {
                finishedTasks.put(i.getKey(), i);
            }
        });
        lastFinishedTaskSize = finishedTasks.size();
        connectSlaves(salveActorUri);
        initStatistical();

    }

    private void connectSlaves(String actorUri) {
        TieBaAnalyserConfigFactory.getInstance().slavesConfig.hosts.iterator().forEachRemaining((String item) -> {
            String path = "akka.tcp://slave@" + item + ":2552" + actorUri;
            ActorSelection queryPageTaskControlActor = actorSystem.actorSelection(path);
            Future<ActorRef> future = queryPageTaskControlActor.resolveOne(Timeout.apply(3, TimeUnit.SECONDS));
            try {
                ActorRef actorRef = Await.result(future, Duration.apply(3, TimeUnit.SECONDS));
                slaves.add(actorRef);
                System.out.println(actorRef.path().toString() + "connect succuss");
            } catch (Exception ex) {
                System.out.println(path + "connect failed");
            }
        });
        System.out.println("finish connect slaves,total:" + slaves.size());
    }

    private void initStatistical() {
        beginTime = System.currentTimeMillis();
        slaves.iterator().forEachRemaining(i -> {
            String key = getHostFromActorPath(i.path().toString());
            slavesRunningTaskMapData.put(key, 0);
            slavesFinishedTaskMapData.put(key, 0);
            slavesFailedTaskMapData.put(key, 0);
        });
    }

    public void putTask(T t) {
        todoTasks.remove(t.getKey());
        excutingTasks.remove(t.getKey());
        finishedTasks.remove(t.getKey());
        failedTasks.remove(t.getKey());

        if (t.status == Task.STATUS_TODO) {
            todoTasks.put(t.getKey(), t);
        } else if (t.status == Task.STATUS_EXCUTING) {
            excutingTasks.put(t.getKey(), t);
        } else if (t.status == Task.STATUS_FINISHED) {
            finishedTasks.put(t.getKey(), t);
        } else if (t.status == Task.STATUS_FAILED) {
            failedTasks.put(t.getKey(), t);
        }
        taskStorage.save(t);
    }

    public void receiveResult(TaskResult result, ActorRef sender) {
        T task = excutingTasks.get(result.getKey());
        if(task==null){
            return;
        }
        String senderPath = getHostFromActorPath(sender.path().toString());

        if (result.success) {
            task.status = Task.STATUS_FINISHED;
            slavesFinishedTaskMapData.put(senderPath, slavesFinishedTaskMapData.get(senderPath) + 1);

        } else {
            task.status = Task.STATUS_FAILED;
            slavesFailedTaskMapData.put(senderPath, slavesFailedTaskMapData.get(senderPath) + 1);
        }
        slavesRunningTaskMapData.put(senderPath, slavesRunningTaskMapData.get(senderPath) - 1);
        putTask(task);
        if ((todoTasks.size() == 0 && excutingTasks.size() == 0)) {
            MailUtil.sendMail("finish!", "finish");
            System.out.println("finish all task! stop indexspider");
            stopManager();
        }
    }

    private void stopManager() {
        cancellables.forEach(i -> {
            i.cancel();
        });
        todoTasks.clear();
        excutingTasks.clear();
        finishedTasks.clear();
        failedTasks.clear();
        slavesRunningTaskMapData.clear();
        slavesFinishedTaskMapData.clear();
        slavesFailedTaskMapData.clear();


        HttpService.getInstance().destroy();
        System.out.println("stop indexspider,total  excuting time:" + (System.currentTimeMillis() - beginTime));
        actorSystem.stop(receiverActor);

    }

    public int getTotalTaskSize() {
        return totalTaskSize;
    }
/**/

    public List<ActorRef> getSlaves() {
        return slaves;
    }


    public void startAlloc(ActorRef actorRef) {
        receiverActor = actorRef;
        System.out.println("total task:"+todoTasks.size());
        cancellables.add(actorSystem.scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                int maxRunningTask = TieBaAnalyserConfigFactory.getInstance().masterConfig.max_running_task;
                if (excutingTasks.size() > maxRunningTask || todoTasks.size() == 0) {
                    return;
                }
                int taskCount = maxRunningTask - excutingTasks.size();
                todoTasks.keySet().stream().sorted((o1, o2) -> {
                    return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
                }).limit(taskCount).collect(Collectors.toList()).forEach(i->{
                    assignTaskToSlave(todoTasks.get(i),receiverActor);
                    todoTasks.remove(i);
                });

            }
        }, actorSystem.dispatcher()));
        initProcessPrinter();
        initFailedChecker();
    }

    private void assignTaskToSlave(T task, ActorRef resultReceiver) {
        if(task==null){
            return;
        }
        ActorRef actorRef = null;
        while ((actorRef = getSlaveToRun()) == null) {
            try {
                Thread.sleep(3000L);
                System.out.println("no avtive slave,wait 3 seconds to try");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        actorRef.tell(task, resultReceiver);
        task.status = PageTask.STATUS_EXCUTING;
        putTask(task);
    }

    private void initProcessPrinter() {
        cancellables.add(actorSystem.scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                printProcess();
            }
        }, actorSystem.dispatcher()));

    }

    private ActorRef getSlaveToRun() {
        if (slaves.size() > 0) {
            String host = slavesRunningTaskMapData.entrySet().stream().min((o1, o2) -> {
                return o1.getValue() - o2.getValue();
            }).get().getKey();

            ActorRef actorRef = slaves.stream().filter(t -> {
                return getHostFromActorPath(t.path().toString()).equals(host);
            }).findAny().get();
            slavesRunningTaskMapData.put(host, slavesRunningTaskMapData.get(host) + 1);
            return actorRef;
        }
        return null;
    }

    public void printProcess() {
        logger.info(buildTotalProcessLogStr() + "\n" + buildSlaveProcessLogStr() + "running info:\n" + "excuting_time:" + (System.currentTimeMillis() - beginTime) / 1000);
    }

    private String buildSlaveProcessLogStr() {
        StringBuilder stringBuilder = new StringBuilder();

        slaves.iterator().forEachRemaining(slave -> {
            DecimalFormat df = new DecimalFormat("######0.00");

            String host = getHostFromActorPath(slave.path().toString());
            int totalTask = slavesFinishedTaskMapData.get(host) + slavesFailedTaskMapData.get(host) + slavesRunningTaskMapData.get(host);
            double speed = slavesFinishedTaskMapData.get(host) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);

            stringBuilder.append(host + ": totalTask:" + totalTask + " progressTask:" + slavesRunningTaskMapData.get(host) + "  finishedTask " + slavesFinishedTaskMapData.get(host) + " failedTask:" + slavesFailedTaskMapData.get(host) + "\nspeed:" + df.format(speed));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }

    private String buildTotalProcessLogStr() {
        DecimalFormat df = new DecimalFormat("######0.00");

        double speed = (finishedTasks.size() - lastFinishedTaskSize) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);
        double time = 1000 * 10000;
        if (speed != 0) {
            time = (totalTaskSize - finishedTasks.size() - failedTasks.size()) / speed;
        }
        return "master info :\ntotalTask:" + totalTaskSize + " lastRunFinished:" + lastFinishedTaskSize + "  unassignedTask:" + todoTasks.size() + " progressTask:" + excutingTasks.size() + "  finishedTask " + finishedTasks.size() + " failedTask:" + failedTasks.size() + " speed:" + df.format(speed) + " resttime:" + df.format(time) + " progress:" + df.format(finishedTasks.size() * 1.0 / totalTaskSize * 100) + "%";
    }

    private void initFailedChecker() {
        Cancellable cancellable = actorSystem.scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(30, TimeUnit.SECONDS), () -> {
            failedFlag = (failedTasks.size() - failedCount) > 20 * slaves.size();
            failedCount = failedTasks.size();
            if (failedFlag) {
                System.out.println("indexspider will stop because of lots of failed task!");
                stopManager();
                MailUtil.sendMail("lots of failed task! please check", "you need check cookie or somthing else.");

            }
        }, actorSystem.dispatcher());
        cancellables.add(cancellable);
    }


}
