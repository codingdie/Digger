package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.DetailTask;
import com.codingdie.analyzer.spider.model.PostIndex;
import com.codingdie.analyzer.spider.model.result.CrawlPostDetailResult;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.task.TaskManager;
import com.codingdie.analyzer.storage.TieBaFileSystem;

/**
 * Created by xupeng on 2017/4/26.
 */
public class DetailSpiderMasterActor extends AbstractActor {


    private TaskManager<DetailTask> taskManager;

    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop DetailSpiderMasterActor");
    }

    public DetailSpiderMasterActor() {
        super();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        HttpService.getInstance().destroy();
        startTaskManager();

    }


    private void startTaskManager() {
        System.out.println("开始初始化存储");
        long tm = System.currentTimeMillis();
        tieBaFileSystem = new TieBaFileSystem(TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name, TieBaFileSystem.ROLE_MASTER);
        taskManager = new TaskManager<DetailTask>(DetailTask.class, tieBaFileSystem, getContext().getSystem(), "/user/DetailSpiderSlaveActor");
        tieBaFileSystem.getPostIndexStorage().iterateNoContentIndex(postIndex -> {
            taskManager.putTask(new DetailTask(postIndex.getPostId()));
        });
        taskManager.startAlloc(getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CrawlPostDetailResult.class, r -> {
            if (r.success) {
                PostIndex postIndex = tieBaFileSystem.getPostIndexStorage().getIndex(r.getPostId());
                postIndex.setStatus(PostIndex.STATUS_HAS_CONTENT);
                postIndex.setContentSlaves(r.getHosts());
                postIndex.setModifyTime(System.currentTimeMillis());
                tieBaFileSystem.getPostIndexStorage().modifyIndex(postIndex);
                System.out.println("finish detail task:" + r.getKey());
            }
            taskManager.receiveResult(r, getSender());

        }).build();
    }




}
