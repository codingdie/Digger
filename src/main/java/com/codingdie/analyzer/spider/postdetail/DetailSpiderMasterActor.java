package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.util.Timeout;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.config.model.WorkConfig;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import com.codingdie.analyzer.storage.spider.IndexSpiderTaskStorage;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.spider.model.PostIndex;
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
public class DetailSpiderMasterActor extends AbstractActor {

    private int totalTask = 0;
    private int lastFinishedTask = 0;
    private int failedCount = 0;
    private boolean failedFlag = false;

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
    private Logger logger = Logger.getLogger("master-task");

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop DetailSpiderMasterActor");
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        initStorage();
        connectSlaves();
        initStatistical();
        startAllocateTask();
        initFailedTimer();


    }
    private void initFailedTimer() {
        callable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(30, TimeUnit.SECONDS), ()->{
            failedFlag=(failedTasks.size()-failedCount)>100;
            failedCount=failedTasks.size();
        },getContext().getSystem().dispatcher());
    }
    private void startAllocateTask() {
        IndexSpiderTaskStorage indexSpiderTaskStorage = tieBaFileSystem.getIndexSpiderTaskStorage();
        List<PageTask> pageTasks = indexSpiderTaskStorage.parseAndRebuild();

        if (pageTasks.size() == 0) {
            Integer totalCount = Integer.valueOf(TieBaAnalyserConfigFactory.getInstance().spiderConfig.totalCount).intValue();
            totalTask = (totalCount - 1) / 50 + 1;
            for (int i = 0; i < totalTask; i++) {
                todoTasks.add(new PageTask(i * 50));
            }
            indexSpiderTaskStorage.saveTasks(todoTasks);
        } else {
            totalTask = pageTasks.size();
            pageTasks.iterator().forEachRemaining(i -> {
                if (i.status != PageTask.STATUS_FINISHED) {
                    todoTasks.add(i);
                } else {
                    finishedTasks.add(i);
                }
            });
            lastFinishedTask = finishedTasks.size();
        }

        callable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {

                int maxRunningTask = TieBaAnalyserConfigFactory.getInstance().masterConfig.max_running_task;

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
        TieBaAnalyserConfigFactory.getInstance().slavesConfig.hosts.iterator().forEachRemaining((String item) -> {
            String path = "akka.tcp://slave@" + item + ":2552/user/DetailSpiderSlaveActor";
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
        SpiderConfig spiderConfig = TieBaAnalyserConfigFactory.getInstance().spiderConfig;
        tieBaFileSystem = new TieBaFileSystem(spiderConfig.tiebaName, TieBaFileSystem.ROLE_MASTER);
        System.out.println("初始化存储完毕");

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class, r -> {
            System.out.println("before:"+tieBaFileSystem.getPostIndexStorage().countAllIndex());
            if (r.success && r.postSimpleInfos != null) {
                System.out.println("new postindex:"+r.postSimpleInfos.stream().filter(i->{return i.type.equals(PostSimpleInfo.TYPE_NORMAL); }).count());

                r.postSimpleInfos.iterator().forEachRemaining(i -> {
                    new Gson().toJson(i);
                    if (i.type.equals(PostSimpleInfo.TYPE_NORMAL)) {
                        PostIndex postIndex = new PostIndex();
                        postIndex.setSpiderHost(getHostFromActorPath(getSender().path().toString()));
                        postIndex.setPostId(i.postId);
                        postIndex.setModifyTime(System.currentTimeMillis());
                        postIndex.setTitle(i.title);
                        postIndex.setPn(r.pn);
                        postIndex.setCreateUser(i.createUser);
                        tieBaFileSystem.getPostIndexStorage().putIndex(postIndex);
                    }

                });

            }
            System.out.println("after:"+tieBaFileSystem.getPostIndexStorage().countAllIndex());


            PageTask pageTask = excutingTasks.stream().filter(queryPageTask -> {
                return queryPageTask.pn == r.pn;
            }).findFirst().get();
            String senderPath = getSender().path().toString();
            excutingTasks.remove(pageTask);
            if (r.success) {
                pageTask.status = PageTask.STATUS_FINISHED;
                slavesFinishedTaskMapData.put(senderPath, slavesFinishedTaskMapData.get(senderPath) + 1);
                finishedTasks.add(pageTask);
            } else {
                pageTask.status = PageTask.STATUS_FAILED;
                slavesFailedTaskMapData.put(senderPath, slavesFailedTaskMapData.get(senderPath) + 1);
                failedTasks.add(pageTask);
            }
            tieBaFileSystem.getIndexSpiderTaskStorage().saveTask(pageTask);
            slavesRunningTaskMapData.put(senderPath, slavesRunningTaskMapData.get(senderPath) - 1);

            if ((todoTasks.size() == 0 && excutingTasks.size() == 0)) {
                System.out.println("finish all task!");
                stopSpider();
            }
            if (failedFlag) {
                System.out.println("因大量失败停止系统");
                stopSpider();
            }
        }).match(PageTask.class, t -> {

            ActorRef queryPageTaskControlActor = null;
            while ((queryPageTaskControlActor = getSlaveToRun()) == null) {
                Thread.sleep(3000L);
                System.out.println("no avtive slave,wait 3 seconds to try");
            }
            queryPageTaskControlActor.tell(t, getSelf());
            t.status = PageTask.STATUS_EXCUTING;
            excutingTasks.add(t);
            tieBaFileSystem.getIndexSpiderTaskStorage().saveTask(t);
            if (todoTasks.size() == 0) {
                callable.cancel();
            }

        }).build();
    }

    private void stopSpider() {
        HttpService.getInstance().destroy();

        System.out.println("stop system,total  excuting time:" + (System.currentTimeMillis() - beginTime));
    }

    private double caculatefailureRate() {
        double f = 0;
        int i = finishedTasks.size() - lastFinishedTask;
        if (i < 100) {
            f = failedTasks.size() > slaves.size() * 5 ? 1.0 : 0.0;
        } else {
            f = failedTasks.size() * 1.0 / i;
        }
        return f;
    }

    private void printProcess() {
        logger.info(buildTotalProcessLogStr() + "\n" + buildSlaveProcessLogStr() + "running info:\n" + "excuting_time:" + (System.currentTimeMillis() - beginTime) / 1000 + " total_post_index:" + tieBaFileSystem.getPostIndexStorage().countAllIndex());
    }

    private String buildSlaveProcessLogStr() {
        StringBuilder stringBuilder = new StringBuilder();

        slaves.iterator().forEachRemaining(slave -> {
            DecimalFormat df = new DecimalFormat("######0.00");

            String key = slave.path().toString();
            String host = getHostFromActorPath(key);
            int totalTask = slavesFinishedTaskMapData.get(key) + slavesFailedTaskMapData.get(key) + slavesRunningTaskMapData.get(key);
            double speed = slavesFinishedTaskMapData.get(key) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);

            stringBuilder.append(host + ": totalTask:" + totalTask + " progressTask:" + slavesRunningTaskMapData.get(key) + "  finishedTask " + slavesFinishedTaskMapData.get(key) + " failedTask:" + slavesFailedTaskMapData.get(key) + "\nspeed:" + df.format(speed));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }

    private String buildTotalProcessLogStr() {
        DecimalFormat df = new DecimalFormat("######0.00");

        double speed = (finishedTasks.size() - lastFinishedTask) * 1.0 / ((System.currentTimeMillis() - beginTime) / 1000);
        double time = 1000 * 10000;
        if (speed != 0) {
            time = (totalTask - finishedTasks.size() - failedTasks.size()) / speed;
        }
        return "master info :\ntotalTask:" + totalTask + " lastRunFinished:" + lastFinishedTask + "  unassignedTask:" + todoTasks.size() + " progressTask:" + excutingTasks.size() + "  finishedTask " + finishedTasks.size() + " failedTask:" + failedTasks.size() + " speed:" + df.format(speed) + " resttime:" + df.format(time) + " progress:" + df.format(finishedTasks.size() * 1.0 / totalTask * 100) + "%";
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
