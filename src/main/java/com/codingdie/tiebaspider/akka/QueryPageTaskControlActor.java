package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageTaskControlActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private  ActorSelection resultCollectActorSelection=null;
    private int pos=0;
    int detail_actor_count=10;

    public static  enum SIGN {SHOW_CHILDCOUNT,STOP}

    @Override
    public void preStart() throws Exception {
        super.preStart();
        detail_actor_count = SpiderConfigFactory.getInstance().slavesConfig.detail_actor_count;
        for(; pos< detail_actor_count; pos++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+pos);
            actorRefList.add(queryPageActor);
        }
        String path = "akka.tcp://master@" + SpiderConfigFactory.getInstance().masterConfig.host + ":2550/user/MasterActor";
        System.out.println(path);
        resultCollectActorSelection = getContext().getSystem().actorSelection(path);
        pos=0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageTask.class, m -> {
            System.out.println(new Gson().toJson(m));

            ActorRef actorRef= actorRefList.get(pos%detail_actor_count);
            actorRef.tell(m,getSelf());
            pos++;
        }).match(QueryPageResult.class,m->{
            resultCollectActorSelection.tell(m,getSelf());
        }).matchEquals(SIGN.STOP,r->{
            getContext().getSystem().terminate();

        }).build();
    }



}
