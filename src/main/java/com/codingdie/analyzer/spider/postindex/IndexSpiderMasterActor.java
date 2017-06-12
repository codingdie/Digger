package com.codingdie.analyzer.spider.postindex;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/26.
 */
public class IndexSpiderMasterActor extends AbstractActor {


    private TaskManager<PageTask> taskManager;

    private List<Cancellable> cancellables = new ArrayList<>();

    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        taskManager=null;
        System.out.println("stop IndexSpiderMasterActor");
    }

    public IndexSpiderMasterActor() {
        super();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        HttpService.getInstance().destroy();
        initStorage();
        startTaskManager();

    }


    private void startTaskManager() {
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

        }).build();
    }


    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }





}
