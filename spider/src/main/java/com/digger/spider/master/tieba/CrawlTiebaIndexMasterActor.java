package com.digger.spider.master.tieba;

import com.digger.config.TieBaAnalyserConfigFactory;
import com.digger.spider.master.tieba.model.model.CrawlTiebaIndexTask;
import com.digger.spider.master.tieba.model.model.PostIndex;
import com.digger.spider.master.tieba.model.result.CrawlTiebaIndexResult;
import com.digger.spider.master.CrawlIndexMasterActor;
import com.digger.spider.network.HttpService;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/26.
 */
public class CrawlTiebaIndexMasterActor extends CrawlIndexMasterActor<PostIndex, CrawlTiebaIndexTask, CrawlTiebaIndexResult> {

    private String tieBaName;

    public CrawlTiebaIndexMasterActor(String tieBaName) {
        this.tieBaName = tieBaName;
    }

    private int queryPageCount() {
        String string = HttpService.getInstance().excute(new Request.Builder()
                .url("https://tieba.baidu.com/f?kw=" + TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name + "&ie=utf-8").build(),"");
        Document document = Jsoup.parse(string);
        return Integer.valueOf(document.select(".last.pagination-item").get(0).attr("href").split("pn=")[1]);
    }

    @Override
    public void onGetIndex(PostIndex index) {

    }

    @Override
    public String getStorageName() {
        return tieBaName;
    }

    @Override
    public Class<CrawlTiebaIndexTask> getIndexTaskClass() {
        return CrawlTiebaIndexTask.class;
    }

    @Override
    public Class<PostIndex> getIndexClass() {
        return PostIndex.class;
    }

    @Override
    public Class<CrawlTiebaIndexResult> getIndexTaskResultClass() {
        return CrawlTiebaIndexResult.class;
    }

    @Override
    public List<CrawlTiebaIndexTask> initIndexTask() {
        List<CrawlTiebaIndexTask> list = new ArrayList<>();
        Integer totalCount = queryPageCount();
        int totalPage = (totalCount - 1) / 50 + 1;
        for (int i = 0; i < totalPage; i++) {
            CrawlTiebaIndexTask indexTask = new CrawlTiebaIndexTask(i * 50);
            indexTask.setTiebaName(tieBaName);
            list.add(indexTask);
        }
        return list;
    }



}
