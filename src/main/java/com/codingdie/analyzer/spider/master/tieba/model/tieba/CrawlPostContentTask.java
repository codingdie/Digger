package com.codingdie.analyzer.spider.master.tieba.model.tieba;

import com.codingdie.analyzer.spider.slave.tieba.CrawlPostDetailActor;

/**
 * Created by xupeng on 2017/4/19.
 */
public class CrawlPostContentTask extends TieBaTask {

    public CrawlPostContentTask(long postId) {
        this.postId=postId;
    }
    public long postId=50;


    @Override
    public String taskId() {
        return String.valueOf(postId);
    }

    @Override
    public String excutorName() {
        return CrawlPostDetailActor.class.getTypeName();
    }


}
