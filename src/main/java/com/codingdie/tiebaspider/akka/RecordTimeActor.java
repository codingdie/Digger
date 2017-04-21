package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import com.codingdie.tiebaspider.model.PostSimpleInfo;
import okhttp3.OkHttpClient;
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
public class RecordTimeActor extends AbstractActor {
    private OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.MINUTES).build();

    private long begin = -1l;
    private long end = -1l;

    public static  enum SIGN {BEGIN, END}

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(
                SIGN.BEGIN, m -> {
                    begin = System.currentTimeMillis();
                }).matchEquals(SIGN.END, m -> {
            end = System.currentTimeMillis();
            System.out.println("time:"+(end-begin));

        }).build();
    }



}
