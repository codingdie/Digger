package com.codingdie.analyzer.spider.master.tieba;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.master.tieba.model.result.CrawlPostDetailResult;
import com.codingdie.analyzer.spider.master.tieba.model.tieba.CrawlPostContentTask;
import com.codingdie.analyzer.spider.master.tieba.model.tieba.PostIndex;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.tieba.TieBaFileSystem;
import com.codingdie.analyzer.task.TaskManager;
import com.codingdie.analyzer.task.model.TaskResult;

/**
 * Created by xupeng on 2017/4/26.
 */
public class CrawlTiebaContentMasterActor extends AbstractActor {


    private TaskManager<CrawlPostContentTask> taskManager;

    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop CrawlTiebaContentMasterActor");
    }

    public CrawlTiebaContentMasterActor() {
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
        taskManager = new TaskManager<CrawlPostContentTask>(CrawlPostContentTask.class, tieBaFileSystem, getContext().getSystem(), "/user/CrawlContentSlaveActor");
        tieBaFileSystem.getIndexStorage().iterateNoContentIndex(postIndex -> {
            taskManager.putTask(new CrawlPostContentTask(postIndex.getPostId()));
        });
        taskManager.startAlloc(getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskResult.class, r -> {
            if (r.success) {
                CrawlPostDetailResult result = (CrawlPostDetailResult) r;
                PostIndex postIndex = tieBaFileSystem.getIndexStorage().getIndex(result.taskId());
                postIndex.setStatus(PostIndex.STATUS_HAS_CONTENT);
                postIndex.setContentSlaves(result.getHosts());
                postIndex.setModifyTime(System.currentTimeMillis());
                tieBaFileSystem.getIndexStorage().modifyIndex(postIndex);
            }
            taskManager.receiveResult(r, getSender());

        }).build();
    }




}
