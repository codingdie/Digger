package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.message.QueryPageMessage;
import com.codingdie.tiebaspider.akka.message.QueryPostDetailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryDetailTaskControlActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private int pos=0;
    private  int totalChildCount=30;
    @Override
    public void preStart() throws Exception {
        super.preStart();
        for(;pos<totalChildCount;pos++){
            ActorRef queryPostDetailActor = context().actorOf(Props.create(QueryPostDetailActor.class), "QueryPostDetailActor"+pos);
            actorRefList.add(queryPostDetailActor);
        }
        pos=0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPostDetailMessage.class, m -> {
            ActorRef actorRef= actorRefList.get(pos%totalChildCount);
            actorRef.tell(m,getSelf());
            pos++;
        }).build();
    }



}
