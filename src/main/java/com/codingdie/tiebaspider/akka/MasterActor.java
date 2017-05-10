package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.util.Timeout;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.codingdie.tiebaspider.storage.SpiderWriter;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xupeng on 2017/4/26.
 */
public class MasterActor extends AbstractActor {

    private int totalPage = 0;

    private List<QueryPageTask> unAssignPagetask = new ArrayList<>();
    private List<QueryPageTask> progressTasks = new ArrayList<>();
    private List<QueryPageTask> finishedtasks = new ArrayList<>();
    private List<QueryPageTask> failedTasks = new ArrayList<>();

    private List<ActorRef> slaves = new ArrayList<>();
    private SpiderWriter spiderWriter;
    private long beginTime = 0;
    private Cancellable callable;
    private ConcurrentHashMap<String, Integer> slavesRunningTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFinishedTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFailedTaskMapData = new ConcurrentHashMap<>();

    private ReentrantLock reentrantLock = new ReentrantLock();
    private Logger logger=Logger.getLogger("master-task");

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop MasterActor");
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        SpiderConfigFactory.getInstance().slavesConfig.hosts.iterator().forEachRemaining((String item) -> {
            String path = "akka.tcp://slave@" + item + ":2552/user/QueryPageTaskControlActor";
            ActorSelection queryPageTaskControlActor = getContext().getSystem().actorSelection(path);
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
        slaves.iterator().forEachRemaining(i -> {
            slavesRunningTaskMapData.put(i.path().toString(), 0);
            slavesFinishedTaskMapData.put(i.path().toString(), 0);
            slavesFailedTaskMapData.put(i.path().toString(), 0);
        });
        beginTime = System.currentTimeMillis();
        spiderWriter = new SpiderWriter(SpiderConfigFactory.getInstance().targetConfig.path);
        Integer totalCount = Integer.valueOf(SpiderConfigFactory.getInstance().targetConfig.totalCount);
        totalPage = (totalCount - 1) / 50 + 1;
        for (int i = 0; i < totalPage; i++) {
            unAssignPagetask.add(new QueryPageTask(i * 50));
        }
        System.out.println("totalPageTaskSize:" + unAssignPagetask.size());
        callable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {

                int maxRunningTask = SpiderConfigFactory.getInstance().masterConfig.max_running_task;

                if (progressTasks.size() > maxRunningTask) {
                    return;
                }
                reentrantLock.lock();

                int taskCount = maxRunningTask - progressTasks.size();

                for (int i = 0; i < taskCount; i++) {
                    getSelf().tell(unAssignPagetask.get(i), getSelf());
                }
                for (int i = 0; i < taskCount; i++) {
                    unAssignPagetask.remove(0);
                }
                reentrantLock.unlock();
            }
        }, getContext().getSystem().dispatcher());
        getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {

                printProcess();
            }
        }, getContext().getSystem().dispatcher());


    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class, r -> {
            if (r.success && r.postSimpleInfos != null) {
                r.postSimpleInfos.iterator().forEachRemaining(i -> {
                    spiderWriter.write(new Gson().toJson(i));
                });
                spiderWriter.flush();

            }
            QueryPageTask pageTask = progressTasks.stream().filter(queryPageTask -> {
                return queryPageTask.pn == r.pn;
            }).findFirst().get();
            String senderPath = getSender().path().toString();
            progressTasks.remove(pageTask);
            if (r.success) {
                slavesFinishedTaskMapData.put(senderPath,slavesFinishedTaskMapData.get(senderPath) +1);
                finishedtasks.add(pageTask);
            } else {
                slavesFailedTaskMapData.put(senderPath,slavesFailedTaskMapData.get(senderPath) +1);
                failedTasks.add(pageTask);
            }
            slavesRunningTaskMapData.put(senderPath, slavesRunningTaskMapData.get(senderPath) - 1);
            if (unAssignPagetask.size() == 0 && progressTasks.size() == 0) {
                spiderWriter.flush();
                slaves.iterator().forEachRemaining(item -> {
                    item.tell(QueryPageTaskControlActor.SIGN.STOP, ActorRef.noSender());
                });
                getContext().getSystem().terminate();
                System.out.println("finish all task,total time:" + (System.currentTimeMillis() - beginTime));
            }
        }).match(QueryPageTask.class, t -> {

            ActorRef queryPageTaskControlActor = null;
            while ((queryPageTaskControlActor = getSlaveToRun()) == null) {
                Thread.sleep(3000L);
                System.out.println("no avtive slave,wait 3 seconds to try");
            }
            queryPageTaskControlActor.tell(t, getSelf());
            t.path = queryPageTaskControlActor.path().toString();
            progressTasks.add(t);
            if (unAssignPagetask.size() == 0) {
                callable.cancel();
            }

        }).build();
    }

    private void printProcess() {
        logger.info(buildTotalProcessLogStr()+"\n"+ buildSlaveProcessLogStr());
    }

    private String buildSlaveProcessLogStr() {
        StringBuilder stringBuilder=new StringBuilder();

        slaves.iterator().forEachRemaining(slave -> {
            DecimalFormat df = new DecimalFormat("######0.00");

            String key = slave.path().toString();
            String host=key.split("@")[1].split(":")[0];
            int totalTask=slavesFinishedTaskMapData.get(key)+slavesFailedTaskMapData.get(key)+slavesRunningTaskMapData.get(key);
            double speed = slavesFinishedTaskMapData.get(key) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);
            double time = Double.MAX_VALUE;
            if (speed != 0) {
                time = (totalPage - finishedtasks.size() - failedTasks.size()) / speed;
            }
            stringBuilder.append(host+": totalTask:" + totalTask  + " progressTask:" + slavesRunningTaskMapData.get(key) + "  finishedTask " + slavesFinishedTaskMapData.get(key) + " failedTask:" + slavesFailedTaskMapData.get(key) + " speed:" + df.format(speed) );
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    private String buildTotalProcessLogStr() {
        DecimalFormat df = new DecimalFormat("######0.00");

        double speed = finishedtasks.size() * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);
        double time = Double.MAX_VALUE;
        if (speed != 0) {
            time = (totalPage - finishedtasks.size() - failedTasks.size()) / speed;
        }

        return "totalTask:" + totalPage + "  unassignedTask:" + unAssignPagetask.size() + " progressTask:" + progressTasks.size() + "  finishedTask " + finishedtasks.size() + " failedTask:" + failedTasks.size() + " speed:" + df.format(speed)+ "  pass:" + (System.currentTimeMillis() - beginTime) / 1000 + " resttime:" + df.format(time) + " progress:" + df.format(finishedtasks.size() * 1.0 / totalPage * 100) + "%" ;
    }




    private ActorRef getSlaveToRun() {
        if (slaves.size() > 0) {
            String path = slavesRunningTaskMapData.entrySet().stream().min((o1, o2) -> {
                return o1.getValue() - o2.getValue();
            }).get().getKey();

            ActorRef actorRef = slaves.stream().filter(t -> {
                return t.path().toString().equals(path);
            }).findAny().get();
            slavesRunningTaskMapData.put(path, slavesRunningTaskMapData.get(path) + 1);
            return actorRef;
        }
        return null;
    }
}
