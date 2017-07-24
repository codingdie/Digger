package com.codingdie.analyzer.spider.model.tieba;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PostDetailTask extends TieBaTask {

    public PostDetailTask(long postId) {
        this.postId=postId;
    }
    public long postId=50;


    @Override
    public String taskId() {
        return String.valueOf(postId);
    }

    @Override
    public String excutorName() {
        return CrawlContentSlaveActor.class.getTypeName();
    }


}
