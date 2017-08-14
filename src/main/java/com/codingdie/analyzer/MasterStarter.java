package com.codingdie.analyzer;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.Member;
import com.codingdie.analyzer.cluster.ClusterListenerActor;
import com.codingdie.analyzer.cluster.ClusterManager;
import com.codingdie.analyzer.common.redis.LocalRedisManager;
import com.codingdie.analyzer.config.AkkaConfigBuilder;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.master.MasterControllServer;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xupeng on 2017/4/24.
 */
public class MasterStarter {
    static Logger logger = Logger.getLogger("master-control");

    public static void main(String[] args) throws Exception {

        LocalRedisManager.start("./build/redis").whenComplete((process, throwable) -> {
            if (args.length > 0) {
                TieBaAnalyserConfigFactory.configFolder = args[0];
            }
            LocalRedisManager.getJedis().keys("*").forEach(s -> {
                System.out.println(s + ":" + LocalRedisManager.getJedis().get("xupeng"));
            });
            TieBaAnalyserConfigFactory.getInstance();
            final ActorSystem system = ActorSystem.create("cluster", new AkkaConfigBuilder().consoleParam(args).roles(Arrays.asList("master")).build());
            ClusterManager.init(system);
            system.actorOf(Props.create(ClusterListenerActor.class), "testActor");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Set<Member> unreachable = Cluster.get(system).state().getUnreachable();
                    Cluster.get(system).state().getMembers().forEach(member -> {
                        if (!unreachable.contains(member)) {
                            logger.info(member.address().toString() + "\t" + member.getRoles().toArray()[0]);
                        }
                    });
                }
            }, 0, 3000l);
            new MasterControllServer().start(system);
        });


    }


}
