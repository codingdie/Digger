package com.codingdie.analyzer.spider.master;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.ContentTask;
import com.codingdie.analyzer.spider.model.PostIndex;
import com.codingdie.analyzer.spider.model.result.CrawlPostDetailResult;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.TieBaFileSystem;

/**
 * Created by xupeng on 2017/4/26.
 */
public class CrawlContentMasterActor extends AbstractActor {


    private TaskManager<ContentTask> taskManager;

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
        taskManager = new TaskManager<ContentTask>(ContentTask.class, tieBaFileSystem, getContext().getSystem(), "/user/CrawlContentSlaveActor");
        tieBaFileSystem.getPostIndexStorage().iterateNoContentIndex(postIndex -> {
            taskManager.putTask(new ContentTask(postIndex.getPostId()));
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
