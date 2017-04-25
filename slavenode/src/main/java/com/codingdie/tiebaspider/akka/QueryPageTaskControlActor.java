package com.codingdie.tiebaspider.akka;

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
public class QueryPageTaskControlActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private int pos=0;
    @Override
    public void preStart() throws Exception {
        super.preStart();
        for(;pos<10;pos++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+pos);
            actorRefList.add(queryPageActor);
        }
        pos=0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageMessage.class, m -> {
            ActorRef actorRef= actorRefList.get(pos%10);
            actorRef.tell(m,getSelf());
            pos++;
        }).build();
    }



}
