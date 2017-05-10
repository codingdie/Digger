package com.codingdie.analyzer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.analyzer.spider.akka.SpiderMasterActor;
import com.codingdie.analyzer.spider.config.ConfigUtil;
import com.codingdie.analyzer.spider.config.SpiderConfigFactory;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xupeng on 2017/4/24.
 */
public class MasterStarter {


    public static void main(String[] args) throws Exception {
        if (initApplicationConfig(args)) {
            String configStr = initAkkaStartParam(args);
            Config config = ConfigFactory.parseString(configStr);
            ActorSystem system = ActorSystem.create("master", config);
            ActorRef resultCollectActorRef = system.actorOf(Props.create(SpiderMasterActor.class), "SpiderMasterActor");
        }
    }


    private static boolean initApplicationConfig(String[] args) throws IOException {
        String configFolder = "";
        if (args.length > 0) {
            configFolder = args[0];
        }
        ConfigUtil.initConfig(configFolder);
        if (SpiderConfigFactory.getInstance().slavesConfig.hosts == null || SpiderConfigFactory.getInstance().slavesConfig.hosts.size() == 0) {
            System.out.println("找不到配置文件或未配置slave节点");
            return false;
        }
        System.out.println(new Gson().toJson(SpiderConfigFactory.getInstance()));

        return true;
    }

    private static String initAkkaStartParam(String[] args) throws IOException {
        String host = "127.0.0.1";
        String port = "2550";

        if (args.length > 1) {
            host = args[1];
        }
        if (args.length > 2) {
            port = args[2];
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("server-application.conf")));
        String configStr = "";
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("hostname = \"127.0.0.1\"")) {
                line = line.replace("hostname = \"127.0.0.1\"", "hostname = \"" + host + "\"");
            }
            if (line.contains("port = 2550")) {
                line = line.replace("port = 2550", "port = " + port);
            }
            configStr += line;
            configStr += "\n";
        }
        System.out.println(configStr);
        return configStr;
    }


}
