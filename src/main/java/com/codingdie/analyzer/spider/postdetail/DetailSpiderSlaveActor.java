package com.codingdie.analyzer.spider.postdetail;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.codingdie.analyzer.config.SpiderConfigFactory;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.postindex.QueryPageActor;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class DetailSpiderSlaveActor extends AbstractActor {

    private  List<ActorRef> actorRefList=new ArrayList<>();
    private  ActorSelection resultCollectActorSelection=null;
    private int totalTaskCount =0;
    int detail_actor_count=10;
    private int finishedTaskCount =0;
    Logger logger=Logger.getLogger("slave-task");
    public static  enum SIGN {STOP}

    @Override
    public void preStart() throws Exception {
        super.preStart();
        detail_actor_count = SpiderConfigFactory.getInstance().slavesConfig.detail_actor_count;
        for(; totalTaskCount < detail_actor_count; totalTaskCount++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+ totalTaskCount);
            actorRefList.add(queryPageActor);
        }
        String path = "akka.tcp://master@" + SpiderConfigFactory.getInstance().masterConfig.host + ":2550/user/DetailSpiderMasterActor";
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

        }).matchEquals(SIGN.STOP, r->{
            HttpService.getInstance().destroy();

            getContext().getSystem().terminate();

        }).build();
    }

    private void printProcess() {
        logger.info("finishedTask:"+ finishedTaskCount +" "+"totalTask:" + totalTaskCount +" restTaskCount:"+(totalTaskCount- finishedTaskCount));
    }


}
