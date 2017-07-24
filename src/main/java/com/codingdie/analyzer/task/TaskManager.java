package com.codingdie.analyzer.task;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.util.Timeout;
import com.codingdie.analyzer.cluster.ClusterManager;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.tieba.PageTask;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.TaskStorage;
import com.codingdie.analyzer.storage.tieba.TieBaFileSystem;
import com.codingdie.analyzer.task.model.Task;
import com.codingdie.analyzer.task.model.TaskResult;
import com.codingdie.analyzer.util.MailUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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

    public TaskManager(Class<T> tClass, TieBaFileSystem tieBaFileSystem, ActorSystem actorSystem, String salveActorUri) {
        this.actorSystem = actorSystem;
        this.taskStorage = tieBaFileSystem.getTaskStorage(tClass);
        List<T> list = taskStorage.parseAndRebuild();
        totalTaskSize = list.size();
        list.iterator().forEachRemaining(i -> {
            if (i.status != T.STATUS_FINISHED) {
                todoTasks.put(i.taskId(), i);
            } else {
                finishedTasks.put(i.taskId(), i);
            }
        });
        lastFinishedTaskSize = finishedTasks.size();
        beginTime = System.currentTimeMillis();

    }


    public void putTasks(List<T> ts) {
        ts.forEach(i -> {
            putTask(i);
        });
    }

    public void putTask(T t) {
        todoTasks.remove(t.taskId());
        excutingTasks.remove(t.taskId());
        finishedTasks.remove(t.taskId());
        failedTasks.remove(t.taskId());

        if (t.status == Task.STATUS_TODO) {
            todoTasks.put(t.taskId(), t);
        } else if (t.status == Task.STATUS_EXCUTING) {
            excutingTasks.put(t.taskId(), t);
        } else if (t.status == Task.STATUS_FINISHED) {
            finishedTasks.put(t.taskId(), t);
        } else if (t.status == Task.STATUS_FAILED) {
            failedTasks.put(t.taskId(), t);
        }
        taskStorage.save(t);
    }

    public void receiveResult(TaskResult result, ActorRef sender) {
        System.out.println("finish  task " + result.taskId() + ":" + result.success);

        T task = excutingTasks.get(result.taskId());
        if (task == null) {
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


    public void startAlloc(ActorRef actorRef) {
        receiverActor = actorRef;
        cancellables.add(actorSystem.scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                int maxRunningTask = TieBaAnalyserConfigFactory.getInstance().masterConfig.max_running_task;
                if (excutingTasks.size() > maxRunningTask || todoTasks.size() == 0) {
                    return;
                }
                if (todoTasks.isEmpty()) {
                    putTasks(getTaskWhenTaskPoolEmpty());
                }
                int taskCount = maxRunningTask - excutingTasks.size();
                todoTasks.keySet().stream().sorted((o1, o2) -> {
                    return Long.valueOf(o1).compareTo(Long.valueOf(o2));
                }).limit(taskCount).collect(Collectors.toList()).forEach(i -> {
                    assignTaskToSlave(todoTasks.get(i), receiverActor);
                    todoTasks.remove(i);
                });

            }
        }, actorSystem.dispatcher()));
        initProcessPrinter();
        initFailedChecker();
    }

    private void assignTaskToSlave(T task, ActorRef resultReceiver) {
        if (task == null) {
            return;
        }
        ActorRef actorRef = getSlaveToRun();
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
        ActorRef actorRef = null;
        while (actorRef == null) {
            initMapData();
            String host = slavesRunningTaskMapData.entrySet().stream().min((o1, o2) -> {
                return o1.getValue() - o2.getValue();
            }).get().getKey();
            if (StringUtils.isNoneBlank(host)) {
                ActorSelection actorSelection = actorSystem.actorSelection(host + "/TaskReceiver");
                Future<ActorRef> future = actorSelection.resolveOne(new Timeout(10, TimeUnit.SECONDS));
                try {
                    actorRef = Await.result(future, new FiniteDuration(10, TimeUnit.SECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (actorRef != null) {
                    slavesRunningTaskMapData.put(host, slavesRunningTaskMapData.get(host) + 1);
                    return actorRef;
                }
            }
            try {
                System.out.println("no salves, wating slave connect to excute task");
                Thread.sleep(3000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;


    }

    private void initMapData() {
        List<String> activeSlaves = ClusterManager.Instance().getActiveSlaves();
        for (String slavePath : activeSlaves) {
            if (!slavesRunningTaskMapData.containsKey(slavePath)) {
                slavesRunningTaskMapData.put(slavePath, 0);
                slavesFinishedTaskMapData.put(slavePath, 0);
                slavesFailedTaskMapData.put(slavePath, 0);
            }
        }
        slavesFailedTaskMapData.keySet().stream().filter(s -> {
            return activeSlaves.contains(s);
        }).collect(Collectors.toSet()).forEach(s -> {
            slavesFailedTaskMapData.remove(s);
            slavesFinishedTaskMapData.remove(s);
            slavesFailedTaskMapData.remove(s);
        });
    }

    public void printProcess() {
        logger.info(buildTotalProcessLogStr() + "\n" + buildSlaveProcessLogStr() + "running info:\n" + "excuting_time:" + (System.currentTimeMillis() - beginTime) / 1000);
    }

    private String buildSlaveProcessLogStr() {
        StringBuilder stringBuilder = new StringBuilder();
        slavesRunningTaskMapData.keySet().iterator().forEachRemaining(slave -> {
            DecimalFormat df = new DecimalFormat("######0.00");
            String host = getHostFromActorPath(slave);
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
            failedFlag = (failedTasks.size() - failedCount) > 20 * slavesRunningTaskMapData.keySet().size();
            failedCount = failedTasks.size();
            if (failedFlag) {
                System.out.println("indexspider will stop because of lots of failed task!");
                stopManager();
                MailUtil.sendMail("lots of failed task! please check", "you need check cookie or somthing else.");

            }
        }, actorSystem.dispatcher());
        cancellables.add(cancellable);
    }

    public List<T> getTaskWhenTaskPoolEmpty() {
        return new ArrayList<>();
    }
}
