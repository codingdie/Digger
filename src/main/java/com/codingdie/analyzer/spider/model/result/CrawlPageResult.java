package com.codingdie.analyzer.spider.model.result;

import com.codingdie.analyzer.spider.model.PostSimpleInfo;
import com.codingdie.analyzer.spider.task.TaskResult;

import java.util.List;

/**
 * Created by xupeng on 2017/4/27.
 */
public class CrawlPageResult extends TaskResult {
    public List<PostSimpleInfo> postSimpleInfos;
    public long  pn;


    @Override
    public String getKey() {
        return String.valueOf(pn);
    }
}
