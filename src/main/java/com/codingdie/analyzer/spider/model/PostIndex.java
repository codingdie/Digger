package com.codingdie.analyzer.spider.model;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/5/10.
 */
public class PostIndex implements Serializable {
    public final static int  STATUS_NO_CONTENT=0;
    public final static int  STATUS_HAS_CONTENT=1;
    public final static int  STATUS_DELETE=2;

    private long postId;
    private long pn;
    private String spiderHost;
    private int  status=STATUS_NO_CONTENT;
    private long modifyTime=System.currentTimeMillis();
    private String title;
    private String createUser;

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getSpiderHost() {
        return spiderHost;
    }

    public void setSpiderHost(String spiderHost) {
        this.spiderHost = spiderHost;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public long getPn() {
        return pn;
    }

    public void setPn(long pn) {
        this.pn = pn;
    }
}
