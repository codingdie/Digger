package storage;

import com.codingdie.analyzer.spider.master.tieba.model.model.PostIndex;
import com.codingdie.analyzer.storage.IndexStorage;
import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xupeng on 2017/5/10.
 */
public class TaskStorageTest extends TestCase {

    public void testA() {

        IndexStorage taskStorage = new IndexStorage<PostIndex>(new File("/media/software/code/j2ee and android/tieba-analyzer/storage/justice_eternal/index"), PostIndex.class);
        System.out.println(taskStorage.countAllIndex());

    }

    public static void main(String[] args) throws InterruptedException {

        IndexStorage taskStorage = new IndexStorage<PostIndex>(new File("/media/software/code/j2ee and android/tieba-analyzer/storage/justice_eternal/index"), PostIndex.class);
        AtomicLong atomicLong = new AtomicLong();
        AtomicInteger integer = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    long begin = System.currentTimeMillis();
                    System.out.println(begin);
                    taskStorage.countAllIndex();
                    atomicLong.getAndAdd(System.currentTimeMillis() - begin);
                    integer.incrementAndGet();
                    System.out.println("end");

                }
            });
        }
        while (integer.get() != 10) {
            System.out.println(integer.get());
            Thread.sleep(3000L);
        }
        System.out.println(atomicLong.get() / 10);

    }

}
