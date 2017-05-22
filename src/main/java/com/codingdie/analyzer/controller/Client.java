package com.codingdie.analyzer.controller;

import akka.actor.*;
import akka.util.Timeout;
import com.codingdie.analyzer.config.AkkaConfigUtil;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/5/16.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        String path = "akka.tcp://master@06.codingdie.com:2552/user/MasterController";
        ActorSystem actorSystem=ActorSystem.create("client", AkkaConfigUtil.initAkkaConfig("127.0.0.1",12982));
        ActorSelection selection = actorSystem.actorSelection(path);
        scala.concurrent.Future<ActorRef> refFuture= selection.resolveOne(Timeout.apply(1, TimeUnit.SECONDS));
        ActorRef actorRef = Await.result(refFuture, Duration.apply(3, TimeUnit.SECONDS));
        actorRef.tell(MasterController.SIGN.INDEX_SPIDER,ActorRef.noSender());
        actorSystem.terminate();
    }

}
