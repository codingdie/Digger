package storage;

import com.codingdie.analyzer.spider.master.tieba.model.model.PostIndex;
import com.codingdie.analyzer.storage.IndexStorage;
import junit.framework.TestCase;

import java.io.File;

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

        System.out.println(taskStorage.countAllIndex());
    }

}
