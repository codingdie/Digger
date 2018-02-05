package com.digger.storage;

import com.digger.redis.LocalRedisManager;
import com.digger.spider.master.tieba.model.model.PostIndex;
import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xupeng on 2017/5/10.
 */
public class TaskStorageTest extends TestCase {

    public void testA() {

        IndexStorage taskStorage = new IndexStorage<PostIndex>(new File("/media/software/code/j2ee and android/tieba-analyzer/storage/justice_eternal/index"), PostIndex.class);
        System.out.println(taskStorage.countAllIndex());

    }

    public static void main(String[] args) throws Exception {
        LocalRedisManager.start("./build/redis").whenComplete((process, throwable) -> {
            IndexStorage taskStorage = new IndexStorage<PostIndex>(new File("/media/software/code/j2ee and android/tieba-analyzer/storage/justice_eternal/index"), PostIndex.class);
            int TOTAL = 30;
            ExecutorService executorService = Executors.newFixedThreadPool(TOTAL);
            CountDownLatch countDownLatch = new CountDownLatch(TOTAL);
            long begin = System.currentTimeMillis();

            for (int i = 0; i < TOTAL; i++) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        taskStorage.countAllIndex();
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println((System.currentTimeMillis() - begin));
            System.out.println((System.currentTimeMillis() - begin) / TOTAL);
        });


    }

}
