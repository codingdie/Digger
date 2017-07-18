package storage;


import com.codingdie.analyzer.spider.postdetail.CrawlPostDetailActor;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

/**
 * Created by xupeng on 17-7-18.
 */
public class CrawlPostDetailActorTest extends TestCase {
    public void testA() {
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(CrawlPostDetailActor.crawlPostDetail(5168285418L)));
    }

}