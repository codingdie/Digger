package com.codingdie.analyzer.spider.slave;


import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.BalancingPool;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.ContentTask;
import com.codingdie.analyzer.spider.model.result.CrawlPostDetailResult;
import com.codingdie.analyzer.spider.network.HttpService;
import org.apache.log4j.Logger;

import static akka.pattern.PatternsCS.ask;

/**
 * Created by xupeng on 2017/4/14.
 */
public class CrawlContentSlaveActor extends AbstractActor {

    private ActorRef router = null;
    private int totalTaskCount = 0;
    int DETAIL_ACTOR_COUNT = 10;
    private int finishedTaskCount = 0;
    Logger logger = Logger.getLogger("slave-detail-task");

    public static enum SIGN {STOP}

    @Override
    public void preStart() throws Exception {
        super.preStart();
        DETAIL_ACTOR_COUNT = TieBaAnalyserConfigFactory.getInstance().slavesConfig.detail_actor_count;
        router = context().actorOf(Props.create(CrawlPostDetailActor.class).withRouter(new BalancingPool(DETAIL_ACTOR_COUNT)));
        totalTaskCount = 0;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ContentTask.class, m -> {
            final ActorRef sender = sender();
            final ActorRef self = getSelf();

            ask(router, m, 500000).whenComplete((result, throwable) -> {
                if (result == null) {

                    sender.tell(CrawlPostDetailResult.TimeOut(m.postId), self);
                } else {
                    sender.tell(result, self);
                }
                finishedTaskCount++;
                printProcess();
            });
            totalTaskCount++;
            printProcess();
        }).matchEquals(SIGN.STOP, r -> {
            HttpService.getInstance().destroy();
        }).build();
    }

    private void printProcess() {
        logger.info("finishedTask:" + finishedTaskCount + " " + "totalTask:" + totalTaskCount + " restTaskCount:" + (totalTaskCount - finishedTaskCount));
    }


}
