package com.codingdie.analyzer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.analyzer.config.AkkaConfigBuilder;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.slave.CrawIndexSlaveActor;

import java.util.Arrays;

/**
 * Created by xupeng on 2017/4/14.
 */
public class SlaveStarter {
    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("cluster", new AkkaConfigBuilder().consoleParam(args).roles(Arrays.asList("slave")).build());
        TieBaAnalyserConfigFactory.getInstance();
        ActorRef indexSpiderSlaveActor = system.actorOf(Props.create(CrawIndexSlaveActor.class), "CrawIndexSlaveActor");
        ActorRef detailSpiderSlaveActor = system.actorOf(Props.create(CrawlContentSlaveActor.class), CrawlContentSlaveActor.class.getSimpleName());
        System.out.println(detailSpiderSlaveActor.path().toString());
        System.out.println(indexSpiderSlaveActor.path().toString());
    }


}
