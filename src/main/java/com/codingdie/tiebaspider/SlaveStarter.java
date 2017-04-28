package com.codingdie.tiebaspider;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.QueryDetailTaskControlActor;
import com.codingdie.tiebaspider.akka.QueryPageTaskControlActor;
import com.codingdie.tiebaspider.config.ConfigUtil;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;

/**
 * Created by xupeng on 2017/4/14.
 */
public class SlaveStarter {
    public static void main(String[] args) throws Exception{
        SpiderConfigFactory.getInstance().masterConfig= ConfigUtil.initMasterConfig(args);
        SpiderConfigFactory.getInstance().targetConfig=ConfigUtil.initTargetConfig(args);
        SpiderConfigFactory.getInstance().slavesConfig=ConfigUtil.initSlavesConfig(args);
        System.out.println(new Gson().toJson(SpiderConfigFactory.getInstance()));

        ActorSystem system = ActorSystem.create("slave", ConfigFactory.load("slave-application.conf"));
        ActorRef queryPageTaskControlActor = system.actorOf(Props.create(QueryPageTaskControlActor.class), "QueryPageTaskControlActor");
        ActorRef queryDetailTaskControlActor = system.actorOf(Props.create(QueryDetailTaskControlActor.class), "QueryDetailTaskControlActor");
        System.out.println(queryPageTaskControlActor.path().toString());
        System.out.println(queryDetailTaskControlActor.path().toString());

    }
}
