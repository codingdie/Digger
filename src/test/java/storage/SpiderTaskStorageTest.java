package storage;

import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.storage.domain.PostIndex;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

import java.io.File;
import java.text.NumberFormat;

/**
 * Created by xupeng on 2017/5/10.
 */
public class SpiderTaskStorageTest extends TestCase {

    public void testAddTasks() {
        TieBaFileSystem tieBaFileSystem = new TieBaFileSystem("李毅1", TieBaFileSystem.ROLE_MASTER);
        tieBaFileSystem.getSpiderTaskStorage().saveTask(new Gson().fromJson("{\"pn\":18300,\"status\":2}", PageTask.class));

        tieBaFileSystem.getSpiderTaskStorage().saveTask(new Gson().fromJson("{\"pn\":18300,\"status\":2}", PageTask.class));

        tieBaFileSystem.getSpiderTaskStorage().parseAndRebuild();
        tieBaFileSystem.clear();

    }

    public void testPostIndex() {
        TieBaFileSystem tieBaFileSystem = new TieBaFileSystem("李毅1", TieBaFileSystem.ROLE_MASTER);
        for (int i = 0; i < 1000; i++) {
            PostIndex postIndex = new PostIndex();
            postIndex.setPostId(i);
            postIndex.setHost("host" + i);
            postIndex.setModifyTime(System.currentTimeMillis());
            tieBaFileSystem.getPostIndexStorage().putIndex(postIndex);
        }
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(tieBaFileSystem.getPostIndexStorage().getIndex(999)));
        tieBaFileSystem.clear();

    }

    public void testNumber() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(2);
        format.setMinimumIntegerDigits(2);
        System.out.println(format.format(1));
    }
}
