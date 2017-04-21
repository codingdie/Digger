package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
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
public class QueryPostDetailActor extends AbstractActor {

    public static final Integer DONE = 9;
    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();

    @Override
    public void postStop() throws Exception {

        super.postStop();
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPostDetailMessage.class, m -> {
            Thread.sleep(2000L);
            getContext().stop(getSelf());
        }).build();
    }


}
