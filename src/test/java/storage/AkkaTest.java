package storage;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.AskableActorSelection;
import akka.pattern.AskableActorSelection$;
import akka.util.Timeout;
import com.codingdie.analyzer.config.AkkaConfigUtil;
import com.codingdie.analyzer.controller.MasterController;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import junit.framework.TestCase;
import org.yaml.snakeyaml.Yaml;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase{
    public  void testA() throws Exception{
        ActorSystem masterActorSystem=ActorSystem.create("master", AkkaConfigUtil.initAkkaConfig("127.0.0.1",1080));
        masterActorSystem.actorOf(Props.create(MasterController.class), "MasterController");
        Thread.sleep(1000*60*60L);
    }
    public  void testB() throws Exception{
        String path = "akka.tcp://master@127.0.0.1:1080/user/MasterController";

        connect(path);

        Thread.sleep(1000*60*60L);
    }
    private void connect(String path) throws Exception {
        ActorSystem clientActorSystem=ActorSystem.create("client", AkkaConfigUtil.initAkkaConfig("127.0.0.1",1981));

        ActorSelection selection = clientActorSystem.actorSelection(path);
        scala.concurrent.Future<ActorRef> refFuture= selection.resolveOne(Timeout.apply(1, TimeUnit.SECONDS));
        ActorRef actorRef = Await.result(refFuture, Duration.apply(3, TimeUnit.SECONDS));
        actorRef.tell("测试",ActorRef.noSender());
        clientActorSystem.terminate();
    }
}
