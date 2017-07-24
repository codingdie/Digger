package storage;


import com.codingdie.analyzer.spider.master.tieba.model.tieba.CrawlPostContentTask;
import com.codingdie.analyzer.spider.slave.tieba.CrawlPostDetailActor;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

/**
 * Created by xupeng on 17-7-18.
 */
public class CrawlPostDetailActorTest extends TestCase {
    public void testA() {
        CrawlPostContentTask crawlPostContentTask = new CrawlPostContentTask(5234422711L);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(CrawlPostDetailActor.crawlPostDetail(crawlPostContentTask)));
    }

}