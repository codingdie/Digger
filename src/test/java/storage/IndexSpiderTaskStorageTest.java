package storage;

import com.codingdie.analyzer.spider.network.HtmlParser;
import com.codingdie.analyzer.config.ConfigUtil;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.storage.TieBaFileSystem;
import com.codingdie.analyzer.spider.model.PostIndex;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import okhttp3.Request;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * Created by xupeng on 2017/5/10.
 */
public class IndexSpiderTaskStorageTest extends TestCase {

    public void testB() throws Exception{


        System.out.println("5072279654".split("\\?")[0]);
    }
    public void testA(){
        ConfigUtil.initConfig("conf");
        String html =HttpService.getInstance().excute(new Request.Builder().url(buildUrl(186350)).build());
        System.out.println(HtmlParser.parseList(html).size());
    }

    private String buildUrl(int pn) {
        return "https://tieba.baidu.com/f?kw=李毅&ie=utf-8&pn=" + pn;
    }
    public void testAddTasks() {
        TieBaFileSystem tieBaFileSystem = new TieBaFileSystem("李毅1", TieBaFileSystem.ROLE_MASTER);
        tieBaFileSystem.getIndexSpiderTaskStorage().saveTask(new Gson().fromJson("{\"pn\":18300,\"status\":2}", PageTask.class));

        tieBaFileSystem.getIndexSpiderTaskStorage().saveTask(new Gson().fromJson("{\"pn\":18300,\"status\":2}", PageTask.class));

        tieBaFileSystem.getIndexSpiderTaskStorage().parseAndRebuild();
        tieBaFileSystem.clear();

    }

    public void testPostIndex() {
        TieBaFileSystem tieBaFileSystem = new TieBaFileSystem("李毅1", TieBaFileSystem.ROLE_MASTER);
        for (int i = 0; i < 1000; i++) {
            PostIndex postIndex = new PostIndex();
            postIndex.setPostId(i);
            postIndex.setSpiderHost("host" + i);
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
