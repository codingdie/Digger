package com.codingdie.analyzer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.analyzer.config.AkkaConfigUtil;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.controller.MasterController;
import com.codingdie.analyzer.config.ConfigUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.log4j.Logger;
import scala.collection.immutable.Iterable;
import scala.compat.java8.functionConverterImpls.FromJavaConsumer;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.AbstractFunction1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/24.
 */
public class MasterStarter {
    static  Logger logger=Logger.getLogger("master-control");

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            TieBaAnalyserConfigFactory.configFolder = args[0];
        }
        final  ActorSystem system = ActorSystem.create("master", AkkaConfigUtil.initAkkaConfigWithConsoleParam(args));
        system.actorOf(Props.create(MasterController.class), "MasterController");
        system.scheduler().schedule(FiniteDuration.apply(1, TimeUnit.SECONDS), FiniteDuration.apply(3, TimeUnit.SECONDS),()->{

            Iterable<ActorRef> actorRefs= system.provider().guardian().children();
            actorRefs.foreach(new FromJavaConsumer<ActorRef>(i->{
                logger.info(i.path().toString());
            }));

        },system.dispatcher());

    }




}
