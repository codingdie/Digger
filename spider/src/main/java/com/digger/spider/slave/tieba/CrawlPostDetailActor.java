package com.digger.spider.slave.tieba;

import akka.actor.AbstractActor;
import com.digger.config.AkkaConfigBuilder;
import com.codingdie.digger.spider.master.tieba.model.model.*;
import com.digger.spider.master.tieba.model.result.CrawlPostDetailResult;
import com.digger.spider.network.HttpService;
import com.digger.spider.master.tieba.model.model.*;
import com.digger.storage.ContentStorage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class CrawlPostDetailActor extends AbstractActor {

    private ContentStorage contentStorage;

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CrawlPostContentTask.class, m -> {
            PostDetail postDetail = crawlPostDetail(m);
            CrawlPostDetailResult result = new CrawlPostDetailResult();
            result.setPostId(m.postId);
            if (postDetail != null) {
                contentStorage.saveContent(postDetail);
                result.success = true;
                result.getHosts().add(AkkaConfigBuilder.getCurHost());
            } else {
                result.errorReason("CrawlDetailError");
            }
            getSender().tell(result, getSelf());


        }).build();
    }

    public static PostDetail crawlPostDetail(CrawlPostContentTask task) {
        String result = HttpService.getInstance().excute(new Request.Builder().url("https://tieba.baidu.com/p/" + task.postId).build(), task.cookie);
        if (StringUtil.isBlank(result)) {
            return null;
        }
        PostDetail postDetail = parseDetail(result, null);
        if (postDetail != null) {
            postDetail.setPostId(task.postId);
            for (int i = 2; i <= postDetail.getPageCount(); i++) {
                result = HttpService.getInstance().excute(new Request.Builder().url("https://tieba.baidu.com/p/" + task.postId + "?pn=" + i).build(), task.cookie);
                parseDetail(result, postDetail);
            }
            postDetail.getFloors().forEach(postFloor -> {
                if (postFloor.getCommentNum() == 0) return;
                postFloor.setRemarks(crawlRemarks(postFloor, task));
            });
        }

        return postDetail;

    }


    private static PostDetail parseDetail(String result, PostDetail post) {
        try {
            Document document = Jsoup.parse(result);
            if (post == null) {
                post = new PostDetail();
                Element totalPage = document.select(".pb_footer ul li").first().select("a").last();
                post.setPageCount(totalPage == null ? 1 : Integer.valueOf(totalPage.attr("href").split("pn=")[1]));
                post.setTitle(document.select(".core_title_wrap_bright h3").text());
            }
            final PostDetail resultPost = post;
            document.select(".l_post").iterator().forEachRemaining(el -> {
                try {
                    PostFloor postFloor = new PostFloor();
                    String data = el.attr("data-field");
                    JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
                    JsonObject authorJson = jsonObject.getAsJsonObject("author");
                    PostAuthor author = new PostAuthor();
                    author.setUserName(authorJson.get("user_name").getAsString());
                    author.setUserId(authorJson.get("user_id").getAsString());
                    postFloor.setAuthor(author);
                    JsonObject contentJson = jsonObject.getAsJsonObject("content");
                    Elements tails = el.select(".post-tail-wrap .tail-info");
                    postFloor.setTime(tails.last().text());
                    postFloor.setContent(contentJson.get("content").getAsString());
                    postFloor.setAnonym(contentJson.get("is_anonym").getAsBoolean());
                    postFloor.setForumId(contentJson.get("forum_id").getAsString());
                    postFloor.setPostId(contentJson.get("post_id").getAsString());
                    postFloor.setThreadId(contentJson.get("thread_id").getAsString());
                    postFloor.setPostIndex(contentJson.get("post_no").getAsInt());
                    postFloor.setType(contentJson.get("type").getAsString());
                    postFloor.setCommentNum(contentJson.get("comment_num").getAsInt());

                    if (postFloor.getPostIndex() == 1 && resultPost.getAuthor() == null) {
                        resultPost.setAuthor(postFloor.getAuthor());
                        resultPost.setTime(postFloor.getTime());
                    }
                    resultPost.getFloors().add(postFloor);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            return resultPost;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return post;
    }

    public static List<PostRemark> crawlRemarks(PostFloor postFloor, CrawlPostContentTask task) {
        List<PostRemark> resultList = new ArrayList<>();
        int i = 0;
        while (resultList.size() < postFloor.getCommentNum()) {
            String result = HttpService.getInstance().excute(new Request.Builder().url("https://tieba.baidu.com/p/comment?tid=" + postFloor.getThreadId() + "&pid=" + postFloor.getPostId() + "&pn=" + i).build(), task.cookie);
            i++;
            List<PostRemark> remarks = parseRemarks(result);
            if (remarks.size() == 0) break;
            resultList.addAll(remarks);
        }
        return resultList;

    }

    private static List<PostRemark> parseRemarks(String result) {
        ArrayList<PostRemark> list = new ArrayList<>();

        if (result == null) return list;
        Document document = Jsoup.parseBodyFragment(result);
        Elements lielements = document.select(".lzl_single_post");
        if (lielements.size() == 0) return list;
        lielements.stream().forEach(el -> {
            PostRemark postRemark = new PostRemark();
            postRemark.setFrom(el.select(".at.j_user_card").text());
            if (el.select(".lzl_content_main").size() == 0) System.out.println(result);
            Element main = el.select(".lzl_content_main").get(0);

            Elements al = main.select(".at");
            if (al.size() > 0) {
                postRemark.setTo(al.get(0).text());
                al.get(0).remove();
            }

            String[] strings = main.text().split(":", 2);
            postRemark.setContent(strings[strings.length - 1]);
            String[] split = main.html().split(":", 2);
            postRemark.setHtmlContent(split[split.length - 1]);
            postRemark.setTime(el.select(".lzl_time").text());
            list.add(postRemark);
        });
        return list;
    }

}
