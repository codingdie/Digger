package com.codingdie.tiebaspider;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.MasterActor;
import com.codingdie.tiebaspider.akka.result.TestActor;
import com.codingdie.tiebaspider.akka.result.TestActor1;
import com.codingdie.tiebaspider.config.*;
import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;

/**
 * Created by xupeng on 2017/4/24.
 */
public class ServerStarter {


    public static void main(String[] args) throws Exception {
        ConfigUtil.initConfig(args);
        System.out.println(new Gson().toJson(SpiderConfigFactory.getInstance()));
        if (SpiderConfigFactory.getInstance().slavesConfig.hosts == null||SpiderConfigFactory.getInstance().slavesConfig.hosts.size()==0) {
            System.out.println("找不到配置文件或未配置slave节点");
            return;
        }
        ActorSystem system = ActorSystem.create("master", ConfigFactory.load("server-application.conf"));
        ActorRef resultCollectActorRef =system.actorOf(Props.create(MasterActor.class),"MasterActor");


    }



}
