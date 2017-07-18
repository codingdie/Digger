package com.codingdie.analyzer.spider.model.result;

import com.codingdie.analyzer.spider.task.TaskResult;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xupeng on 2017/4/21.
 */
public class CrawlPostDetailResult extends TaskResult {
    private long postId = -1;
    private List<String> hosts;

    @Override
    public String getKey() {
        return String.valueOf(postId);
    }

    public CrawlPostDetailResult(long postId, List<String> hosts) {
        this.postId = postId;
        this.hosts = hosts;
    }

    public long getPostId() {
        return postId;
    }

    public List<String> getHosts() {
        return hosts;
    }
}
