package com.codingdie.analyzer.spider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.akka.result.QueryPageResult;
import com.codingdie.analyzer.spider.config.SpiderConfigFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageTaskControlActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private  ActorSelection resultCollectActorSelection=null;
    private int totalTaskCount =0;
    int detail_actor_count=10;
    private int finishedTaskCount =0;
    Logger logger=Logger.getLogger("slave-task");
    public static  enum SIGN {SHOW_CHILDCOUNT,STOP}

    @Override
    public void preStart() throws Exception {
        super.preStart();
        detail_actor_count = SpiderConfigFactory.getInstance().slavesConfig.detail_actor_count;
        for(; totalTaskCount < detail_actor_count; totalTaskCount++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+ totalTaskCount);
            actorRefList.add(queryPageActor);
        }
        String path = "akka.tcp://master@" + SpiderConfigFactory.getInstance().masterConfig.host + ":2550/user/SpiderMasterActor";
        System.out.println(path);
        resultCollectActorSelection = getContext().getSystem().actorSelection(path);
        totalTaskCount =0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PageTask.class, m -> {

            ActorRef actorRef= actorRefList.get(totalTaskCount %detail_actor_count);
            actorRef.tell(m,getSelf());
            totalTaskCount++;
            printProcess();

        }).match(QueryPageResult.class,m->{

            finishedTaskCount++;
            resultCollectActorSelection.tell(m,getSelf());
            printProcess();

        }).matchEquals(SIGN.STOP,r->{
            getContext().getSystem().terminate();

        }).build();
    }

    private void printProcess() {
        logger.info("finishedTask:"+ finishedTaskCount +" "+"totalTask:" + totalTaskCount +" restTaskCount:"+(totalTaskCount- finishedTaskCount));
    }


}
