package com.codingdie.analyzer.spider.model.tieba;

/**
 * Created by xupeng on 17-7-21.
 */
public class PostAuthor {
    private String userId;
    private String userName;
    private String props;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }
}
