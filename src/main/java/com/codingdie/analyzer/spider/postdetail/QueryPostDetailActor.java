package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.spider.model.DetailTask;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPostDetailActor extends AbstractActor {


    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DetailTask.class, m -> {


        }).build();
    }


}
