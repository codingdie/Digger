package com.codingdie.analyzer.cluster;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xupeng on 17-7-24.
 */
public class ClusterManager {
    public static final String ROLE_SLAVE = "slave";
    private static ClusterManager clusterManager;
    private Cluster cluster;

    public static synchronized void init(ActorSystem actorSystem) {
        if (clusterManager == null) {
            clusterManager = new ClusterManager();
            clusterManager.cluster = Cluster.get(actorSystem);
        }
    }

    public static ClusterManager Instance() {
        return clusterManager;
    }

    public List<String> getActiveSlaves() {
        Set<Member> members = cluster.state().getUnreachable();
        List<String> activeMembers = new ArrayList<>();
        cluster.state().getMembers().forEach(member -> {
            if (!members.contains(member) && member.hasRole(ROLE_SLAVE)) {
                activeMembers.add(member.address().toString());
            }
        });
        return activeMembers;
    }
}
