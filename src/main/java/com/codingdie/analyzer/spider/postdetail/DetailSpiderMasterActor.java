package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.model.PostIndex;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import com.codingdie.analyzer.spider.task.TaskManager;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.util.MailUtil;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/26.
 */
public class DetailSpiderMasterActor extends AbstractActor {

    private int failedCount = 0;
    private boolean failedFlag = false;
    private TaskManager<PageTask> taskManager;

    private List<Cancellable> cancellables = new ArrayList<>();

    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop IndexSpiderMasterActor");
    }

    public DetailSpiderMasterActor() {
        super();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        HttpService.getInstance().destroy();
        initStorage();
        start();
        initFailedChecker();
        initProcessPrinter();

    }

    private void initProcessPrinter() {
        Cancellable cancellable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                taskManager.printProcess();
            }
        }, getContext().getSystem().dispatcher());
        cancellables.add(cancellable);
    }

    private void initFailedChecker() {
        Cancellable cancellable = getContext().getSystem().scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(30, TimeUnit.SECONDS), () -> {
            failedFlag = (taskManager.getFailedTasks().size() - failedCount) > 20 * taskManager.getSlaves().size();
            failedCount = taskManager.getFailedTasks().size();
        }, getContext().getSystem().dispatcher());
        cancellables.add(cancellable);
    }

    private void start() {
        taskManager = new TaskManager<PageTask>(tieBaFileSystem,getContext().getSystem(),"/user/IndexSpiderSlaveActor");
        if (taskManager.getTotalTaskSize() == 0) {
            Integer totalCount = Integer.valueOf(TieBaAnalyserConfigFactory.getInstance().spiderConfig.total_count).intValue();
            int totalPage = (totalCount - 1) / 50 + 1;
            for (int i = 0; i < totalPage; i++) {
                taskManager.putTask(new PageTask(i * 50));
            }
        }
        taskManager.startAlloc(getSelf());
    }





    private void initStorage() {
        long tm = System.currentTimeMillis();
        System.out.println("开始初始化存储");
        SpiderConfig spiderConfig = TieBaAnalyserConfigFactory.getInstance().spiderConfig;
        tieBaFileSystem = new TieBaFileSystem(spiderConfig.tieba_name, TieBaFileSystem.ROLE_MASTER);
        System.out.println("初始化存储完毕用时:" + (System.currentTimeMillis() - tm));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class, r -> {
            if (r.success && r.postSimpleInfos != null) {
                r.postSimpleInfos.iterator().forEachRemaining(i -> {
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
            taskManager.receiveResult(r,getSender());



            if ((taskManager.getTodoTasks().size() == 0 && taskManager.getExcutingTasks().size() == 0)) {
                MailUtil.sendMail("finish!", "finish");
                System.out.println("finish all task! stop indexspider");
                stopSpider();
            }
            if (failedFlag) {
                MailUtil.sendMail("lots of failed task! please check", "you need check cookie or somthing else.");
                System.out.println("indexspider will stop because of lots of failed task!");
                stopSpider();
            }
        }).build();
    }

    private void stopSpider() {
        HttpService.getInstance().destroy();
        cancellables.forEach(i -> {
            i.cancel();
        });
        getContext().stop(self());
        System.out.println("stop indexspider,total  excuting time:" + (System.currentTimeMillis() - taskManager.getBeginTime()));
    }

    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }





}
