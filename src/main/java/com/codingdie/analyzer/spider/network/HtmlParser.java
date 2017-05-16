package com.codingdie.analyzer.spider.network;

import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/5/12.
 */
public class HtmlParser {
    public static  List<PostSimpleInfo> parseList(String string) {
        Document document = Jsoup.parse(string);
        List<PostSimpleInfo> postSimpleInfos = new ArrayList<>();

        document.select("#thread_list .j_thread_list").iterator().forEachRemaining(el -> {
            PostSimpleInfo postSimpleInfo = new PostSimpleInfo();

            try {
                postSimpleInfo.remarkNum = Integer.valueOf(el.select(".threadlist_rep_num").text());
                postSimpleInfo.createUser = el.select(".tb_icon_author").text();
                postSimpleInfo.lastUpdateUser = el.select(".frs-author-name").text();
                String text = el.select(".threadlist_reply_date").text();
                postSimpleInfo.lastUpdateTime = text.contains(":") ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : text;
            }catch (Exception ex){
                ex.printStackTrace();
            }
            try {
                postSimpleInfo.postId = Long.valueOf(el.select(".threadlist_title a").attr("href").split("/")[2].split("\\?")[0]) ;
                postSimpleInfo.title = el.select(".threadlist_title a").text();
            } catch (Exception ex) {
                ex.printStackTrace();
                postSimpleInfo.type = PostSimpleInfo.TYPE_UNKONWN;
            } finally {
                postSimpleInfos.add(postSimpleInfo);
            }
        });
        return postSimpleInfos;
    }

}
