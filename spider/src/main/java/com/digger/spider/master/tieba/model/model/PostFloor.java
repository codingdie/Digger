package com.digger.spider.master.tieba.model.model;

import java.util.List;

/**
 * Created by xupeng on 17-7-18.
 */
public class PostFloor {

    private String postId;

    private int postIndex;
    private String content;
    private  String clientType;
    private  String time;
    private PostAuthor author;
    private int commentNum;
    private String forumId;
    private String threadId;
    private boolean isAnonym;
    private String type;
    private List<PostRemark> remarks;


    public int getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(int postIndex) {
        this.postIndex = postIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public PostAuthor getAuthor() {
        return author;
    }

    public void setAuthor(PostAuthor author) {
        this.author = author;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public boolean isAnonym() {
        return isAnonym;
    }

    public void setAnonym(boolean anonym) {
        isAnonym = anonym;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public List<PostRemark> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<PostRemark> remarks) {
        this.remarks = remarks;
    }
}
