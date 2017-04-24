package com.codingdie.tiebaspider;

import akka.actor.ActorSystem;

/**
 * Created by xupeng on 2017/4/24.
 */
public class ServerNode {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("Hello1");
        system.actorSelection("akka.tcp://Hello@127.0.0.1:2552/");
    }
}
