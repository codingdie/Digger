package com.codingdie.tiebaspider;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.message.QueryPageMessage;
import com.codingdie.tiebaspider.akka.QueryPageActor;
import com.codingdie.tiebaspider.akka.RecordTimeActor;

/**
 * Created by xupeng on 2017/4/14.
 */
public class ChildStarter {
    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("Hello");
        ActorRef queryPageActor = system.actorOf(Props.create(QueryPageActor.class), "QueryPageActor");
        ActorRef recordTimeActor = system.actorOf(Props.create(RecordTimeActor.class), "RecordTimeActor");


        long begin=System.currentTimeMillis();
        recordTimeActor.tell(RecordTimeActor.SIGN.BEGIN,recordTimeActor);
        for(int i=0;i<10;i++){
            queryPageActor.tell(new QueryPageMessage(i*50),queryPageActor);
        }

    }
}
