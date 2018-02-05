package com.codingdie.digger.config;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaConfigBuilder {

    public static final String AKKA_REMOTE_NETTY_TCP_PORT = "akka.remote.netty.tcp.port";
    public static final String AKKA_CLUSTER_ROLES = "akka.cluster.roles";
    public static final String AKKA_REMOTE_NETTY_TCP_HOSTNAME = "akka.remote.netty.tcp.hostname";

    private static String HOST = "127.0.0.1";
    private static List<String> roles = new ArrayList<>();
    private static int PORT = 2552;
    private Config config = null;


    public static String getCurHost() {
        return HOST;
    }

    public static int getCurPort() {
        return PORT;
    }

    public AkkaConfigBuilder() {
        config = ConfigFactory.load(this.getClass().getClassLoader());
        host(HOST);
        port(PORT);
    }

    public AkkaConfigBuilder host(String host) {
        this.HOST = host;
        config = config.withValue(AKKA_REMOTE_NETTY_TCP_HOSTNAME, ConfigValueFactory.fromAnyRef(host));
        return this;
    }

    public AkkaConfigBuilder port(int port) {
        this.PORT = port;
        config = config.withValue(AKKA_REMOTE_NETTY_TCP_PORT, ConfigValueFactory.fromAnyRef(port));
        return this;
    }

    public AkkaConfigBuilder roles(List<String> roles) {
        this.roles = roles;
        config = config.withValue(AKKA_CLUSTER_ROLES, ConfigValueFactory.fromIterable(roles));
        return this;
    }

    public AkkaConfigBuilder consoleParam(String[] args) {
        String host = AkkaConfigBuilder.HOST;
        int port = AkkaConfigBuilder.PORT;

        if (args.length > 1) {
            host = args[1];
        }
        if (args.length > 2) {
            port = Integer.valueOf(args[2]);
        }
        host(host);
        port(port);
        return this;
    }

    public Config build() {
//        config.entrySet().forEach(kv -> {
//            System.out.println(kv.getKey() + "\t" + kv.getValue().render());
//        });
        return config;
    }
}
