package com.codingdie.analyzer.spider.model.tieba;

import com.codingdie.analyzer.storage.model.Index;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/5/10.
 */
public class PostIndex extends Index implements Serializable {

    private long postId;
    private long pn;
    private String title;
    private String createUser;

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
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

    @Override
    public String getIndexId() {
        return String.valueOf(postId);
    }
}
