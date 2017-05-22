package com.codingdie.analyzer.spider;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.network.HtmlParser;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import okhttp3.Request;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import java.util.List;

/**
 * Created by xupeng on 2017/4/14.
 */
public class SpiderConfigUpdateActor extends AbstractActor {

     public  static enum SIGN{COOKIE}
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SpiderConfig.class, i->{
             TieBaAnalyserConfigFactory.getInstance().spiderConfig=i;
        }).build();
    }



}
