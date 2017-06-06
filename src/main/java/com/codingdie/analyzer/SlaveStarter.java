package com.codingdie.analyzer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.analyzer.config.AkkaConfigUtil;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.postindex.IndexSpiderSlaveActor;
import com.typesafe.config.Config;

/**
 * Created by xupeng on 2017/4/14.
 */
public class SlaveStarter {
    public static void main(String[] args) throws Exception {
        Config config = AkkaConfigUtil.initAkkaConfigWithConsoleParam(args);
        ActorSystem system = ActorSystem.create("slave", config);

        TieBaAnalyserConfigFactory.getInstance();
        ActorRef indexSpiderSlaveActor = system.actorOf(Props.create(IndexSpiderSlaveActor.class), "IndexSpiderSlaveActor");

        System.out.println(indexSpiderSlaveActor.path().toString());
    }



}
