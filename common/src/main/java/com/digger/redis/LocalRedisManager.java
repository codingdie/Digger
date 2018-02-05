package com.digger.redis;

import com.digger.util.ProcessUtil;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;

/**
 * Created by xupeng on 17-8-9.
 */
public class LocalRedisManager {
    private static Jedis jedis = null;

    public static CompletableFuture<Process> start(String folder) throws Exception {
        return ProcessUtil.excute("cd " + folder + ";./start-redis.sh", s -> {
            System.out.println(s);
            return s.contains("Redis") && s.contains("just started");
        });
    }

    public synchronized static Jedis getJedis() {
        if (jedis == null) {
            System.out.println("start init redis connection");
            jedis = new Jedis("127.0.0.1");
            System.out.println("end init redis connection");

        }
        return jedis;
    }
}
