package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.message.QueryPostDetailMessage;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
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

    private final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).readTimeout(60, TimeUnit.SECONDS).build();
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageTask.class, m -> {
            String url = "http://tieba.baidu.com/f?kw=" + SpiderConfigFactory.getInstance().targetConfig.tiebaName + "&ie=utf-8&pn=" + m.pn;
            System.out.println(url);
            Request request = new Request.Builder()
                    .url(url)
                    .header("Cookie","TIEBA_USERTYPE=0bea8f495b5b1aafb7172334; bdshare_firstime=1483500517910; SEENKW=steam; IS_NEW_USER=87feed1d0323025218a9bc32; CLIENTWIDTH=525; CLIENTHEIGHT=1210; SET_PB_IMAGE_WIDTH=780; BDUSS=WJ1eXlXRzh6cTIzVHdaZ1JocVNTREZkYmowTlJkRnBLb0tNYjUtSzl2SGo5d3BaSVFBQUFBJCQAAAAAAAAAAAEAAADfKJ9eaGFybW9uaWNhX3gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAONq41jjauNYV; TIEBAUID=1f2164d117dfc73b20d8d179; STOKEN=d4770a7c22c260f0369a239ef021027b8ddfdd8e3f9c9a4418d20fb141c65511; BAIDUID=17758A7A13D171BFCBD2105FFC1E1CE8:FG=1; BIDUPSID=62132CCAB5D624C3C152D6E44E12F82A; PSTM=1492508479; MCITY=-131%3A; bottleBubble=1; 1587488991_FRSVideoUploadTip=1; BDSFRCVID=Pi_sJeCCxG3u9E5ZW9CUeiNaw68A5FUjjEw63J; H_BDCLCKID_SF=tRk8oI0aJDvbfP0kKtbShnLOqxby26Rh-DOLQb7a-JOq-J3624bbq6QBjNtXt6KfJ-_t_D05f-QBs4O_bfbT2Mby5HONtlTUHC6WannLfRu-bhDGQlo8MxLWjGbZqjLefRA8_ILQbRrEDnuk-PvE-PnHMx8X5-RLf2cpa-OF5l8-h4nVLfn2jqIibl58KxTHtR5-aKbG-K3xOKQpyJ8-2hLT2f8jJMQNKGvksRoN3KJmO4P9bT3v5DuRKhoN2-biWbRL2MbdJD5mbC89DT_hj63M5pJfetJKaDOXsJOOa6rjDCvtDp35y4LdjG5tQpcUtIrz-MFE3b5kqCDwbJuh3MAq3-Aq5xcw-DTesR7OKtOJHRT8y5t2QfbQ0MjPqP-jW5ILatcXWn7JOpkxhfnxy5500aCDt5FjJbIqV-35b5raHnRv5t8_-P4DHUjH2-rea5TH3bKB5-30HP0kjjDbjI60jGtfejLqJbksbPTa-hcqEpO9QTbhefQ0WRJtb6bZJ5-fbx-y0C5PSJcFQJt-DUThDHt8JT-tJJ3fL-08-ROSDP5gq4bohj4EKxnLb-Rt-D7-SR35MJObKCKCjTt_jj5WjgIttj-ffKnw5-OF5lOTJh0R5jrpjqIi5tOt54nltKttVpDa3f58jl56j6bke6oWeH0ft6Ksb5vfstjJan7he5rnhPFQbRDehqby-Rj25RjML-cJb5uhMC06q6_-jTbHjHDft5nf-g5eaJ5n0-nnhPKwQjoA3njD5tR8K5caQnbC_lQG2JcpfILRy6CajTcQjN_qqbbfb-oD3bRObR-_Hn7zHPjKhPuyhpoH5-L_55vE3-08JCLbMTuGj6tbq6QBjNtsQPcd-2Q-5hOy3KOhbqKGWltBLpL8MtTK-5KO0RTl-Rc_340-hpFu-n5jHjoLjatq3J; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; PSINO=2; H_PS_PSSID=1456_21104_20719")
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                System.out.println(("Unexpected code " + response));
            } else {
                String string = response.body().string();
                List<PostSimpleInfo> postSimpleInfos = parseResponse(string);
                postSimpleInfos.iterator().forEachRemaining(t->{
                    ActorSelection selection= getContext().actorSelection("/user/QueryDetailTaskControlActor");
                    selection.tell(new QueryPostDetailMessage(t.postId),getSelf());
                });
                System.out.println(postSimpleInfos.size());
                QueryPageResult queryPageResult=new QueryPageResult();
                queryPageResult.postSimpleInfos=postSimpleInfos;
                queryPageResult.pn=m.pn;
                getSender().tell(queryPageResult,getSelf());
            }
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
