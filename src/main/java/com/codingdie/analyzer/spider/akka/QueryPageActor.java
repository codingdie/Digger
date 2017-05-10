package com.codingdie.analyzer.spider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import com.codingdie.analyzer.spider.akka.result.QueryPageResult;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.akka.message.QueryPostDetailMessage;
import com.codingdie.analyzer.spider.config.SpiderConfigFactory;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageActor extends AbstractActor {



    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PageTask.class, m -> {
            String html =HttpService.getInstance().excute(new Request.Builder().url(buildUrl(m.pn)).build());
            QueryPageResult queryPageResult = new QueryPageResult();
            queryPageResult.pn = m.pn;
            if(StringUtil.isBlank(html)){
                queryPageResult.success = false;
            }else{
                List<PostSimpleInfo> postSimpleInfos = parseResponse(html);
                postSimpleInfos.iterator().forEachRemaining(t -> {
                    ActorSelection selection = getContext().actorSelection("/user/QueryDetailTaskControlActor");
                    selection.tell(new QueryPostDetailMessage(t.postId), getSelf());
                });
                queryPageResult.postSimpleInfos = postSimpleInfos;
                queryPageResult.success = true;
            }

            getSender().tell(queryPageResult, getSelf());

        }).build();
    }


    private String buildUrl(int pn) {
        return "http://tieba.baidu.com/f?kw=" + SpiderConfigFactory.getInstance().workConfig.tiebaName + "&ie=utf-8&pn=" + pn;
    }

    private List<PostSimpleInfo> parseResponse(String string) {
        Document document = Jsoup.parse(string);
        List<PostSimpleInfo> postSimpleInfos = new ArrayList<>();

        document.select("#thread_list .j_thread_list").iterator().forEachRemaining(el -> {
            PostSimpleInfo postSimpleInfo = new PostSimpleInfo();

            try {
                postSimpleInfo.remarkNum = Integer.valueOf(el.select(".threadlist_rep_num").get(0).text());
                postSimpleInfo.createUser = el.select(".tb_icon_author a").get(0).text();
                postSimpleInfo.lastUpdateUser = el.select(".frs-author-name").get(0).text();
                String text = el.select(".threadlist_reply_date").get(0).text();
                postSimpleInfo.lastUpdateTime = text.contains(":") ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : text;
                postSimpleInfo.postId = el.select(".threadlist_title a").attr("href").split("/")[2];
                postSimpleInfo.title = el.select(".threadlist_title a").text();
            } catch (Exception ex) {
                postSimpleInfo.type = PostSimpleInfo.TYPE_UNKONWN;
            } finally {
                postSimpleInfos.add(postSimpleInfo);
            }
        });
        return postSimpleInfos;
    }

    public static void main(String[] args) {

    }
}
