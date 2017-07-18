package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.spider.model.DetailTask;
import com.codingdie.analyzer.spider.model.PostDetail;
import com.codingdie.analyzer.spider.model.PostFloor;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.network.HttpService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/14.
 */
public class CrawlPostDetailActor extends AbstractActor {


    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DetailTask.class, m -> {
            PostDetail postDetail=crawlPostDetail(m);
            if(postDetail!=null){
                getSender().tell(postDetail, getSelf());
            }

        }).build();
    }

    public static PostDetail crawlPostDetail(DetailTask task) {
        String result = HttpService.getInstance().excute(new Request.Builder().url("https://tieba.baidu.com/p/" + task.postId).build(), task.cookie);
        if(StringUtil.isBlank(result)){
            return null;
        }
        PostDetail postDetail = parseDetail(result, null);
        for (int i = 2; i <= postDetail.getPageCount(); i++) {
            result = HttpService.getInstance().excute(new Request.Builder().url("https://tieba.baidu.com/p/" + task.postId + "?pn=" + i).build(), task.cookie);
            parseDetail(result, postDetail);
        }
        return postDetail;

    }

    private static PostDetail parseDetail(String result, PostDetail post) {


        Document document = Jsoup.parse(result);
        if (post == null) {
            post = new PostDetail();
            post.setPageCount(Integer.valueOf(document.select(".pb_footer ul li").first().select( "a").last().attr("href").split("pn=")[1]));
            post.setTitle(document.select(".core_title_wrap_bright h3").text());
        }
        ;
        final PostDetail resultPost = post;
        document.select(".l_post").iterator().forEachRemaining(el -> {
            try {
                PostFloor postFloor = new PostFloor();
                postFloor.setAuthor(el.select(".p_author .d_name").get(0).text());
                Elements tails = el.select(".post-tail-wrap .tail-info");
                postFloor.setTime(tails.last().text());
                postFloor.setHtmlContent(el.select("cc").get(0).html());
                postFloor.setTextContent(el.select("cc").get(0).text());
                postFloor.setIndex(Integer.valueOf(tails.get(tails.size() - 2).text().replace("楼", "")));
                if (postFloor.getIndex() == 1 && StringUtil.isBlank(resultPost.getAuthor())) {
                    resultPost.setAuthor(postFloor.getAuthor());
                    resultPost.setTime(postFloor.getTime());
                }
                resultPost.getFloors().add(postFloor);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
        return resultPost;
    }

}
