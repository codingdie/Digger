package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import com.codingdie.tiebaspider.HttpUtil;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.message.QueryPostDetailMessage;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.codingdie.tiebaspider.model.PostSimpleInfo;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageActor extends AbstractActor {

    private OkHttpClient client = HttpUtil.buildClient();


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageTask.class, m -> {
            String html = query(m.pn);
            QueryPageResult queryPageResult = new QueryPageResult();
            queryPageResult.pn = m.pn;
            List<PostSimpleInfo> postSimpleInfos = parseResponse(html);
            postSimpleInfos.iterator().forEachRemaining(t -> {
                ActorSelection selection = getContext().actorSelection("/user/QueryDetailTaskControlActor");
                selection.tell(new QueryPostDetailMessage(t.postId), getSelf());
            });
            queryPageResult.postSimpleInfos = postSimpleInfos;
            queryPageResult.success = true;
            getSender().tell(queryPageResult, getSelf());

        }).build();
    }

    private String query(int pn) {
        String html = null;
        long begin = System.currentTimeMillis();
        int n = 0;
        String url = buildUrl(pn);
        while (html == null || html.length() == 0) {
            try {

                n++;
                Response response = getHttpResponse(url);
                if (response.code() != 200) {
                    String location = response.header("Location");
                    System.out.println("Location:" + location + " code" + response.code() + " url:" + url);
                    response.close();
                    if (location != null && !location.equals(url)) {
                        url = location;
                    }
                    Thread.sleep(10000L);

                } else {
                    html = response.body().string();
                }
                if (n> 3) {
                    System.out.println(pn + "尝试" + n + "次失败,耗时" + (System.currentTimeMillis() - begin));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return html;
    }

    private Response getHttpResponse(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", SpiderConfigFactory.getInstance().targetConfig.cookie)
                .header("Proxy-Authorization", "Basic " + Base64.getEncoder().encodeToString(SpiderConfigFactory.getInstance().masterConfig.key.getBytes()))
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.header("Set-Cookie"));
        System.out.println(response.headers("Set-Cookie").stream().reduce((i,j)->{
            return  i+"/"+j;
        }));

        HttpUtil.newCookie(response.headers("Set-Cookie"));
//        System.out.println(SpiderConfigFactory.getInstance().targetConfig.cookie);

        return response;
    }

    private String buildUrl(int pn) {
        return "http://tieba.baidu.com/f?kw=" + SpiderConfigFactory.getInstance().targetConfig.tiebaName + "&ie=utf-8&pn=" + pn;
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
