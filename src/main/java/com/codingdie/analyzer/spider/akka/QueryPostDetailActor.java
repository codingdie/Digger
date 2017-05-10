package com.codingdie.analyzer.spider.akka;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.spider.akka.message.QueryPostDetailMessage;
import okhttp3.OkHttpClient;

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
//            System.out.println(m.postId);
        }).build();
    }


}
