package com.codingdie.tiebaspider.akka.result;

import akka.actor.AbstractActor;

/**
 * Created by xupeng on 2017/4/28.
 */
public class TestActor extends AbstractActor {
    @Override
    public void postStop() throws Exception {
        super.postStop();

        System.out.println("TestActor stop");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("stop",t->{
            getContext().getSystem().terminate();

        }).build();
    }
}
