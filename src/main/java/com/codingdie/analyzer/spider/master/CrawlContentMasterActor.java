package com.codingdie.analyzer.spider.master;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.result.CrawlPostDetailResult;
import com.codingdie.analyzer.spider.model.tieba.PostDetailTask;
import com.codingdie.analyzer.spider.model.tieba.PostIndex;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.tieba.TieBaFileSystem;
import com.codingdie.analyzer.task.TaskManager;

/**
 * Created by xupeng on 2017/4/26.
 */
public class CrawlContentMasterActor extends AbstractActor {


    private TaskManager<PostDetailTask> taskManager;

    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop CrawlContentMasterActor");
    }

    public CrawlContentMasterActor() {
        super();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        HttpService.getInstance().destroy();
        startTaskManager();

    }


    private void startTaskManager() {
        long tm = System.currentTimeMillis();
        tieBaFileSystem =  TieBaFileSystem.getInstance(TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name, TieBaFileSystem.ROLE_MASTER);
        taskManager = new TaskManager<PostDetailTask>(PostDetailTask.class, tieBaFileSystem, getContext().getSystem(), "/user/CrawlContentSlaveActor");
        tieBaFileSystem.getIndexStorage().iterateNoContentIndex(postIndex -> {
            taskManager.putTask(new PostDetailTask(postIndex.getPostId()));
        });
        taskManager.startAlloc(getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CrawlPostDetailResult.class, r -> {
            if (r.success) {
                PostIndex postIndex = tieBaFileSystem.getIndexStorage().getIndex(r.getPostId());
                postIndex.setStatus(PostIndex.STATUS_HAS_CONTENT);
                postIndex.setContentSlaves(r.getHosts());
                postIndex.setModifyTime(System.currentTimeMillis());
                tieBaFileSystem.getIndexStorage().modifyIndex(postIndex);
                System.out.println("finish detail task:" + r.getKey());
            }
            taskManager.receiveResult(r, getSender());

        }).build();
    }




}
