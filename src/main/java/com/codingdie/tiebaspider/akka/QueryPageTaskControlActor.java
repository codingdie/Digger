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
    private int taskCount =0;
    int detail_actor_count=10;
    private int resultCount=0;

    public static  enum SIGN {SHOW_CHILDCOUNT,STOP}

    @Override
    public void preStart() throws Exception {
        super.preStart();
        detail_actor_count = SpiderConfigFactory.getInstance().slavesConfig.detail_actor_count;
        for(; taskCount < detail_actor_count; taskCount++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+ taskCount);
            actorRefList.add(queryPageActor);
        }
        String path = "akka.tcp://master@" + SpiderConfigFactory.getInstance().masterConfig.host + ":2550/user/MasterActor";
        System.out.println(path);
        resultCollectActorSelection = getContext().getSystem().actorSelection(path);
        taskCount =0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageTask.class, m -> {
            System.out.println(new Gson().toJson(m));

            ActorRef actorRef= actorRefList.get(taskCount %detail_actor_count);
            actorRef.tell(m,getSelf());
            taskCount++;
            System.out.println("taskcount:"+ taskCount);

        }).match(QueryPageResult.class,m->{
            resultCount++;
            System.out.println("resultCount:"+resultCount);
            resultCollectActorSelection.tell(m,getSelf());
        }).matchEquals(SIGN.STOP,r->{
            getContext().getSystem().terminate();

        }).build();
    }



}
