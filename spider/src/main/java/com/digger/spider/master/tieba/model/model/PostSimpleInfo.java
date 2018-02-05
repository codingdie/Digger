package com.digger.spider.master.tieba.model.model;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PostSimpleInfo implements Serializable {

    public static  final String TYPE_NORMAL="normal";
    public static  final String TYPE_UNKONWN="unkonwn";

    private String lastUpdateTime;
    private String lastUpdateUser;
    private String createUser;
    private String title;
    private int remarkNum ;
    private long postId ;
    private String type=TYPE_NORMAL ;

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRemarkNum() {
        return remarkNum;
    }

    public void setRemarkNum(int remarkNum) {
        this.remarkNum = remarkNum;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
