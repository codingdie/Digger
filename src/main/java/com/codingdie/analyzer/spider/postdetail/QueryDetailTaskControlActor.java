package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.akka.message.QueryPostDetailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryDetailTaskControlActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private int pos=0;
    private  int detail_actor_count =30;
    @Override
    public void preStart() throws Exception {
        super.preStart();
        detail_actor_count = TieBaAnalyserConfigFactory.getInstance().slavesConfig.detail_actor_count;

        for(; pos< detail_actor_count; pos++){
            ActorRef queryPostDetailActor = context().actorOf(Props.create(QueryPostDetailActor.class), "QueryPostDetailActor"+pos);
            actorRefList.add(queryPostDetailActor);
        }
        pos=0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPostDetailMessage.class, m -> {
            ActorRef actorRef= actorRefList.get(pos% detail_actor_count);
            actorRef.tell(m,getSelf());
            pos++;
        }).build();
    }



}
