package com.codingdie.digger.spider.master.tieba.model.model;

import com.codingdie.digger.spider.slave.tieba.CrawlPostDetailActor;
import com.codingdie.digger.storage.model.Task;

/**
 * Created by xupeng on 2017/4/19.
 */
public class CrawlPostContentTask extends Task {

    public CrawlPostContentTask(long postId) {
        this.postId=postId;
    }
    public long postId=50;

    private String tiebaName;

    @Override
    public String taskId() {
        return String.valueOf(postId);
    }

    @Override
    public String excutorName() {
        return CrawlPostDetailActor.class.getTypeName();
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getTiebaName() {
        return tiebaName;
    }

    public void setTiebaName(String tiebaName) {
        this.tiebaName = tiebaName;
    }
}
