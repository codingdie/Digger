package com.codingdie.analyzer.spider.postindex;

import akka.actor.AbstractActor;
import com.codingdie.analyzer.spider.network.HtmlParser;
import com.codingdie.analyzer.spider.postindex.result.QueryPageResult;
import com.codingdie.analyzer.spider.network.HttpService;
import com.codingdie.analyzer.spider.model.PageTask;
import com.codingdie.analyzer.config.SpiderConfigFactory;
import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import okhttp3.Request;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import java.util.*;

/**
 * Created by xupeng on 2017/4/14.
 */
public class QueryPageActor extends AbstractActor {

    Logger logger=Logger.getLogger("parse-info");

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PageTask.class, m -> {
            String html =HttpService.getInstance().excute(new Request.Builder().url(buildUrl(m.pn)).build());
            QueryPageResult queryPageResult = new QueryPageResult();
            queryPageResult.pn = m.pn;
            if(StringUtil.isBlank(html)){
                logger.info(m.pn+":html null");
                queryPageResult.success = false;
            }else{
                List<PostSimpleInfo> postSimpleInfos = HtmlParser.parseList(html);
                queryPageResult.postSimpleInfos = postSimpleInfos;
                queryPageResult.success = true;
                long normalCount = queryPageResult.postSimpleInfos.stream().filter(i -> {
                    return i.type.equals(PostSimpleInfo.TYPE_NORMAL);
                }).count();
                if(normalCount==0){
                    queryPageResult.success=false;
                }
                logger.info(m.pn+":"+normalCount);
            }
            getSender().tell(queryPageResult, getSelf());
        }).build();
    }

    private String buildUrl(int pn) {
        return "https://tieba.baidu.com/f?kw=" + SpiderConfigFactory.getInstance().workConfig.tiebaName + "&ie=utf-8&pn=" + pn;
    }

}
