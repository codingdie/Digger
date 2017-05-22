package com.codingdie.analyzer.spider.postindex;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import com.codingdie.analyzer.spider.network.HttpService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xupeng on 2017/4/14.
 */
public class IndexSpiderSlaveActor extends AbstractActor {

    Logger logger=Logger.getLogger("slave-task");
    private  List<ActorRef> pageActors =new ArrayList<>();
    private int totalTaskCount =0;

    private int finishedTaskCount =0;
    public static   enum SIGN {STOP}
    private ConcurrentHashMap<Long,ActorRef> senderMap=new ConcurrentHashMap<>();
    @Override
    public void preStart() throws Exception {
        super.preStart();
        for(; totalTaskCount < TieBaAnalyserConfigFactory.getInstance().slavesConfig.page_actor_count; totalTaskCount++){
            ActorRef queryPageActor = context().actorOf(Props.create(QueryPageActor.class), "QueryPageActor"+ totalTaskCount);
            pageActors.add(queryPageActor);
        }
        totalTaskCount =0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PageTask.class, m -> {

            ActorRef actorRef= pageActors.get(totalTaskCount % TieBaAnalyserConfigFactory.getInstance().slavesConfig.page_actor_count);
            actorRef.tell(m,getSelf());
            senderMap.put(m.pn,getSender());
            totalTaskCount++;
            printProcess();

        }).match(QueryPageResult.class,m->{
            finishedTaskCount++;
            senderMap.get(m.pn).tell(m,getSelf());
            senderMap.remove(m.pn);
            printProcess();

        }).matchEquals(SIGN.STOP,r->{
            HttpService.getInstance().destroy();

        }).build();
    }

    private void printProcess() {
        logger.info("finishedTask:"+ finishedTaskCount +" "+"totalTask:" + totalTaskCount +" restTaskCount:"+(totalTaskCount- finishedTaskCount));
    }


}
