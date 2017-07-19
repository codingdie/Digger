package com.codingdie.analyzer.spider.model.result;

import com.codingdie.analyzer.spider.task.TaskResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/21.
 */
public class CrawlPostDetailResult extends TaskResult {
    private long postId = -1;
    private List<String> hosts=new ArrayList<>();

    @Override
    public String getKey() {
        return String.valueOf(postId);
    }

    public CrawlPostDetailResult() {
        super();
    }

    public long getPostId() {
        return postId;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
}
