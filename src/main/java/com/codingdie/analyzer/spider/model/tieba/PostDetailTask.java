package com.codingdie.analyzer.spider.model.tieba;

import com.codingdie.analyzer.task.model.Task;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PostDetailTask extends Task {

    public PostDetailTask(long postId) {
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
