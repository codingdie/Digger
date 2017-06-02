package com.codingdie.analyzer.spider;

import akka.actor.AbstractActor;
import akka.remote.AssociationErrorEvent;
import com.sun.nio.sctp.AssociationChangeNotification;

/**
 * Created by xupeng on 2017/6/2.
 */
public class AssociateListener extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(AssociationErrorEvent.class, i->{

            System.out.println("AssocChangeEvent:"+i.eventName());
        }).build();
    }
}
