package storage;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.codingdie.analyzer.config.AkkaConfigUtil;
import com.codingdie.analyzer.controller.MasterControllServer;
import junit.framework.TestCase;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase{
     public  void  testA(){
         String a="http://localhost:8081/logs/parse-info.log";
         System.out.println(Pattern.matches(".*/logs/.*.log1",a));
     }
}
