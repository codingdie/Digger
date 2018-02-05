package com.codingdie.digger.cluster;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;

/**
 * Created by xupeng on 17-7-24.
 */
public class ClusterListenerActor extends AbstractActor {
    Cluster cluster = Cluster.get(getContext().getSystem());

    //subscribe to cluster changes
    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterEvent.MemberUp.class, mUp -> {
                    System.out.println("Member is Up: " + mUp.member());
                })
                .match(ClusterEvent.UnreachableMember.class, mUnreachable -> {
                    System.out.println("Member detected as unreachable: " + mUnreachable.member());
                })
                .match(ClusterEvent.MemberRemoved.class, mRemoved -> {
                    System.out.println("Member is Removed: " + mRemoved.member());
                })
                .match(ClusterEvent.MemberEvent.class, message -> {
                    // ignore
                })
                .build();
    }
}
