package com.codingdie.analyzer.spider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.util.Timeout;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.akka.result.QueryPageResult;
import com.codingdie.analyzer.spider.config.SpiderConfigFactory;
import com.codingdie.analyzer.spider.config.WorkConfig;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.storage.SpiderTaskStorage;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.storage.domain.PostIndex;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xupeng on 2017/4/26.
 */
public class SpiderMasterActor extends AbstractActor {

    private int totalPage = 0;
    private int initFinishedCount = 0;

    private List<PageTask> todoTasks = new ArrayList<>();
    private List<PageTask> excutingTasks = new ArrayList<>();
    private List<PageTask> finishedTasks = new ArrayList<>();
    private List<PageTask> failedTasks = new ArrayList<>();

    private List<ActorRef> slaves = new ArrayList<>();
    private long beginTime = 0;
    private Cancellable callable;
    private ConcurrentHashMap<String, Integer> slavesRunningTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFinishedTaskMapData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> slavesFailedTaskMapData = new ConcurrentHashMap<>();
    private TieBaFileSystem tieBaFileSystem;
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Logger logger=Logger.getLogger("master-task");

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop SpiderMasterActor");
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        initStorage();
        connectSlaves();
        initStatistical();
        startAllocateTask();


    }

    private void startAllocateTask() {
        SpiderTaskStorage spiderTaskStorage=tieBaFileSystem.getSpiderTaskStorage();
        List<PageTask> pageTasks= spiderTaskStorage.parseAndRebuild();

        if(pageTasks.size()==0){
            Integer totalCount = Integer.valueOf(SpiderConfigFactory.getInstance().workConfig.totalCount).intValue();
            totalPage = (totalCount - 1) / 50 + 1;
            for (int i = 0; i < totalPage; i++) {
                todoTasks.add(new PageTask(i * 50));
            }
            spiderTaskStorage.saveTasks(todoTasks);
        }else{
            totalPage=pageTasks.size();
            pageTasks.iterator().forEachRemaining(i->{
                if(i.status!=PageTask.STATUS_FINISHED){
                    todoTasks.add(i);
                }else {
                    finishedTasks.add(i);
                }
            });
            initFinishedCount=finishedTasks.size();
        }

        callable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {

                int maxRunningTask = SpiderConfigFactory.getInstance().masterConfig.max_running_task;

                if (excutingTasks.size() > maxRunningTask) {
                    return;
                }
                reentrantLock.lock();

                int taskCount = maxRunningTask - excutingTasks.size();

                for (int i = 0; i < taskCount; i++) {
                    getSelf().tell(todoTasks.get(i), getSelf());
                }
                for (int i = 0; i < taskCount; i++) {
                    todoTasks.remove(0);
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

    private void initStatistical() {
        beginTime = System.currentTimeMillis();

        slaves.iterator().forEachRemaining(i -> {
            slavesRunningTaskMapData.put(i.path().toString(), 0);
            slavesFinishedTaskMapData.put(i.path().toString(), 0);
            slavesFailedTaskMapData.put(i.path().toString(), 0);
        });
    }

    private void connectSlaves() {
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

    }

    private void initStorage() {
        System.out.println("开始初始化存储");
        WorkConfig workConfig = SpiderConfigFactory.getInstance().workConfig;
        tieBaFileSystem=new TieBaFileSystem(workConfig.tiebaName,TieBaFileSystem.ROLE_MASTER);
        System.out.println("初始化存储完毕");

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class, r -> {
            if (r.success && r.postSimpleInfos != null) {
                r.postSimpleInfos.iterator().forEachRemaining(i -> {
                    new Gson().toJson(i);
                    if(i.type.equals(PostSimpleInfo.TYPE_NORMAL)){
                        PostIndex postIndex=new PostIndex();
                        postIndex.setHost(getHostFromActorPath(getSender().path().toString()));
                        postIndex.setPostId(i.postId);
                        postIndex.setModifyTime(System.currentTimeMillis());
                        postIndex.setComment(i.title+":"+i.type);
                        tieBaFileSystem.getPostIndexStorage().putIndex(postIndex);
                    }

                });

            }
            PageTask pageTask = excutingTasks.stream().filter(queryPageTask -> {
                return queryPageTask.pn == r.pn;
            }).findFirst().get();
            String senderPath = getSender().path().toString();
            excutingTasks.remove(pageTask);
            if (r.success) {
                pageTask.status=PageTask.STATUS_FINISHED;
                slavesFinishedTaskMapData.put(senderPath,slavesFinishedTaskMapData.get(senderPath) +1);
                finishedTasks.add(pageTask);
            } else {
                pageTask.status=PageTask.STATUS_FAILED;
                slavesFailedTaskMapData.put(senderPath,slavesFailedTaskMapData.get(senderPath) +1);
                failedTasks.add(pageTask);
            }
            tieBaFileSystem.getSpiderTaskStorage().saveTask(pageTask);
            slavesRunningTaskMapData.put(senderPath, slavesRunningTaskMapData.get(senderPath) - 1);
            if ((todoTasks.size() == 0 && excutingTasks.size() == 0)||failedTasks.size()>20) {
                slaves.iterator().forEachRemaining(item -> {
                    item.tell(QueryPageTaskControlActor.SIGN.STOP, ActorRef.noSender());
                });
                getContext().getSystem().terminate();
                System.out.println("stop system,total  excuting time:" + (System.currentTimeMillis() - beginTime));
            }
        }).match(PageTask.class, t -> {

            ActorRef queryPageTaskControlActor = null;
            while ((queryPageTaskControlActor = getSlaveToRun()) == null) {
                Thread.sleep(3000L);
                System.out.println("no avtive slave,wait 3 seconds to try");
            }
            queryPageTaskControlActor.tell(t, getSelf());
            t.status=PageTask.STATUS_EXCUTING;
            excutingTasks.add(t);
            tieBaFileSystem.getSpiderTaskStorage().saveTask(t);
            if (todoTasks.size() == 0) {
                callable.cancel();
            }

        }).build();
    }

    private void printProcess() {
        logger.info(buildTotalProcessLogStr()+"\n"+ buildSlaveProcessLogStr()+"running info:\n"+"excuting_time:"+(System.currentTimeMillis()-beginTime)/1000+" total_post_index:"+tieBaFileSystem.getPostIndexStorage().countAllIndex());
    }

    private String buildSlaveProcessLogStr() {
        StringBuilder stringBuilder=new StringBuilder();

        slaves.iterator().forEachRemaining(slave -> {
            DecimalFormat df = new DecimalFormat("######0.00");

            String key = slave.path().toString();
            String host= getHostFromActorPath(key);
            int totalTask=slavesFinishedTaskMapData.get(key)+slavesFailedTaskMapData.get(key)+slavesRunningTaskMapData.get(key);
            double speed = slavesFinishedTaskMapData.get(key) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);

            stringBuilder.append(host+": totalTask:" + totalTask  + " progressTask:" + slavesRunningTaskMapData.get(key) + "  finishedTask " + slavesFinishedTaskMapData.get(key) + " failedTask:" + slavesFailedTaskMapData.get(key) + "\nspeed:" + df.format(speed) );
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }

    private String buildTotalProcessLogStr() {
        DecimalFormat df = new DecimalFormat("######0.00");

        double speed = (finishedTasks.size()-initFinishedCount) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);
        double time = 1000*10000;
        if (speed != 0) {
            time = (totalPage - finishedTasks.size() - failedTasks.size()) / speed;
        }
        return "master info :\ntotalTask:" + totalPage + " lastRunFinished:" + initFinishedCount +  "  unassignedTask:" + todoTasks.size() + " progressTask:" + excutingTasks.size() + "  finishedTask " + finishedTasks.size() + " failedTask:" + failedTasks.size() + " speed:" + df.format(speed)+ " resttime:" + df.format(time) + " progress:" + df.format(finishedTasks.size() * 1.0 / totalPage * 100) + "%" ;
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