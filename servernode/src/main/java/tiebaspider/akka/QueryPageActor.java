package tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.message.QueryPageMessage;
import com.codingdie.tiebaspider.akka.message.QueryPostDetailMessage;
import com.codingdie.tiebaspider.akka.result.QueryPostDetailResult;
import com.codingdie.tiebaspider.model.PostSimpleInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageActor extends AbstractActor {

    public static final Integer DONE = 9;
    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();
    public static  enum SIGN {SHOW_CHILDCOUNT}

    @Override
    public void postStop() throws Exception {
        super.postStop();
        getContext().actorSelection(getContext().getSystem().$div("RecordTimeActor")).tell(RecordTimeActor.SIGN.END,getSelf());

    }
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageMessage.class, m -> {
            Request request = new Request.Builder()
                    .url("http://tieba.baidu.com/f?kw=justice_eternal&ie=utf-8&pn="+m.pn)

                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                System.out.println(("Unexpected code " + response));
            } else {
                String string = response.body().string();
                List<PostSimpleInfo> postSimpleInfos = parseResponse(string);
                System.out.println(postSimpleInfos.size());
                postSimpleInfos.iterator().forEachRemaining(t->{
                    ActorRef actorRef= getContext().actorOf(Props.create(QueryPostDetailActor.class));
                    actorRef.tell(new QueryPostDetailMessage(t.postId),getSelf());
                });

            }

        }).match(QueryPostDetailResult.class,queryPostDetailResult -> {

        }).matchEquals(SIGN.SHOW_CHILDCOUNT,r->{
            System.out.println("childSize:"+getContext().children().size());
        }).build();
    }

    private List<PostSimpleInfo> parseResponse(String string) {
        Document document = Jsoup.parse(string);
        List<PostSimpleInfo> postSimpleInfos = new ArrayList<>();

        document.select("#thread_list .j_thread_list").iterator().forEachRemaining(el -> {
            PostSimpleInfo postSimpleInfo = new PostSimpleInfo();

            try {
                postSimpleInfo.remarkNum = Integer.valueOf(el.select(".threadlist_rep_num").get(0).text());
                postSimpleInfo.createUser=el.select(".tb_icon_author a").get(0).text();
                postSimpleInfo.lastUpdateUser=el.select(".frs-author-name").get(0).text();
                String text = el.select(".threadlist_reply_date").get(0).text();
                postSimpleInfo.lastUpdateTime= text.contains(":")?LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE):text;
                postSimpleInfo.postId=el.select(".threadlist_title a").attr("href").split("/")[2];
                postSimpleInfo.title=el.select(".threadlist_title a").text();
            }catch (Exception ex){
                postSimpleInfo.type=PostSimpleInfo.TYPE_UNKONWN;
            }finally {
                postSimpleInfos.add(postSimpleInfo);

            }

        });

        return postSimpleInfos;
    }

}
