package com.codingdie.analyzer.spider.model;

import com.codingdie.analyzer.spider.task.Task;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class DetailTask extends Task {

    public DetailTask(long postId){
        this.postId=postId;
    }
    public long postId=50;

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String getKey() {
        return String.valueOf(postId);
    }
}
