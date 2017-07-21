package com.codingdie.analyzer.spider.slave;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.model.result.CrawlPageResult;
import com.codingdie.analyzer.spider.network.HttpService;
import okhttp3.Request;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageActor extends AbstractActor {

    Logger logger = Logger.getLogger("parse-info");

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PageTask.class, m -> {
            String html = HttpService.getInstance().excute(new Request.Builder().url(buildUrl(m)).build(), m.cookie);
            CrawlPageResult crawlPageResult = new CrawlPageResult();
            crawlPageResult.pn = m.pn;
            if (StringUtil.isBlank(html)) {
                logger.info(m.pn + ":html null");
                crawlPageResult.success = false;
            } else {
                List<PostSimpleInfo> postSimpleInfos = parseList(html);
                crawlPageResult.postSimpleInfos = postSimpleInfos;
                crawlPageResult.success = true;
                long normalCount = crawlPageResult.postSimpleInfos.stream().filter(i -> {
                    return i.getType().equals(PostSimpleInfo.TYPE_NORMAL);
                }).count();
                if (normalCount == 0) {
                    crawlPageResult.success = false;
                }
                logger.info(m.pn + ":" + normalCount);
            }
            getSender().tell(crawlPageResult, getSelf());
        }).build();
    }

    public List<PostSimpleInfo> parseList(String string) {
        Document document = Jsoup.parse(string);
        List<PostSimpleInfo> postSimpleInfos = new ArrayList<>();
        document.select("#thread_list .j_thread_list").iterator().forEachRemaining(el -> {
            PostSimpleInfo postSimpleInfo = new PostSimpleInfo();

            try {
                postSimpleInfo.setRemarkNum(Integer.valueOf(el.select(".threadlist_rep_num").text()));
                postSimpleInfo.setCreateUser(el.select(".tb_icon_author").text());
                postSimpleInfo.setLastUpdateUser(el.select(".frs-author-name").text());
                String text = el.select(".threadlist_reply_date").text();
                postSimpleInfo.setLastUpdateTime(text.contains(":") ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : text);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                postSimpleInfo.setPostId(Long.valueOf(el.select(".threadlist_title a").attr("href").split("/")[2].split("\\?")[0]));
                postSimpleInfo.setTitle(el.select(".threadlist_title a").text());
            } catch (Exception ex) {
                ex.printStackTrace();
                postSimpleInfo.setTitle(PostSimpleInfo.TYPE_UNKONWN);
            } finally {
                postSimpleInfos.add(postSimpleInfo);
            }
        });
        return postSimpleInfos;
    }

    private String buildUrl(PageTask task) {
        return "https://tieba.baidu.com/f?kw=" + task.tiebaName+ "&ie=utf-8&pn=" + task.pn;
    }

}
