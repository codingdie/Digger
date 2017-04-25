package com.codingdie.tiebaspider;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.codingdie.tiebaspider.akka.QueryDetailTaskControlActor;
import com.codingdie.tiebaspider.akka.QueryPageTaskControlActor;
import com.codingdie.tiebaspider.akka.message.QueryPageMessage;
import com.codingdie.tiebaspider.akka.QueryPageActor;
import com.codingdie.tiebaspider.akka.RecordTimeActor;

/**
 * Created by xupeng on 2017/4/14.
 */
public class SlaveStarter {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("slave");
        ActorRef queryPageTaskControlActor = system.actorOf(Props.create(QueryPageTaskControlActor.class), "QueryPageTaskControlActor");
        ActorRef queryDetailTaskControlActor = system.actorOf(Props.create(QueryDetailTaskControlActor.class), "QueryDetailTaskControlActor");
        System.out.println(queryPageTaskControlActor.path().toString());
        System.out.println(queryDetailTaskControlActor.path().toString());

    }
}
