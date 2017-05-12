package com.codingdie.analyzer.storage.domain;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/5/10.
 */
public class PostIndex implements Serializable {
    public final static int  STATUS_NO_CONTENT=0;
    public final static int  STATUS_HAS_CONTENT=1;
    public final static int  STATUS_DELETE=2;

    private long postId;
    private String host;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
}
