package com.codingdie.analyzer.spider.master.tieba;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.master.tieba.model.result.CrawlTiebaIndexResult;
import com.codingdie.analyzer.spider.master.tieba.model.tieba.CrawlTiebaIndexTask;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.tieba.TieBaFileSystem;
import com.codingdie.analyzer.task.TaskManager;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by xupeng on 2017/4/26.
 */
public class CrawlTiebaIndexActor extends AbstractActor {


    private TaskManager<CrawlTiebaIndexTask> taskManager;


    private TieBaFileSystem tieBaFileSystem;


    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop CrawlTiebaIndexActor");
    }

    public CrawlTiebaIndexActor() {
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
        tieBaFileSystem =  TieBaFileSystem.getInstance(TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name, TieBaFileSystem.ROLE_MASTER);
        taskManager = new TaskManager<>(CrawlTiebaIndexTask.class, tieBaFileSystem, getContext().getSystem(), "/user/CrawIndexSlaveActor");
        if (taskManager.getTotalTaskSize() == 0) {
            initPageCountFromNetwork();
            Integer totalCount = Integer.valueOf(TieBaAnalyserConfigFactory.getInstance().spiderConfig.total_count).intValue();
            int totalPage = (totalCount - 1) / 50 + 1;
            for (int i = 0; i < totalPage; i++) {
                taskManager.putTask(new CrawlTiebaIndexTask(i * 50));
            }
        }else{
            TieBaAnalyserConfigFactory.getInstance().spiderConfig.total_count =taskManager.getTotalTaskSize();
        }
        System.out.println("初始化存储完毕用时:" + (System.currentTimeMillis() - tm));
        System.out.println("当前Index数量:" + tieBaFileSystem.getIndexStorage().countAllIndex());

        taskManager.startAlloc(getSelf());
    }

    private void initPageCountFromNetwork() {
        String string = HttpService.getInstance().excute(new Request.Builder()
                .url("https://tieba.baidu.com/f?kw=" + TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name + "&ie=utf-8").build(),"");
        Document document = Jsoup.parse(string);
        TieBaAnalyserConfigFactory.getInstance().spiderConfig.total_count =Integer.valueOf(document.select(".last.pagination-item").get(0).attr("href").split("pn=")[1]) ;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CrawlTiebaIndexResult.class, r -> {
            if (r.success ) {
                r.getIndexes().forEach(postIndex->{
                    tieBaFileSystem.getIndexStorage().putIndex(postIndex);
                });
            }
            taskManager.receiveResult(r,getSender());
        }).build();
    }

    private String getHostFromActorPath(String key) {
        return key.split("@")[1].split(":")[0];
    }





}
