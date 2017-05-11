package com.codingdie.analyzer.spider.akka.message;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPostDetailMessage implements Serializable {
    public QueryPostDetailMessage(long postId){
        this.postId=postId;
    }
    public long postId;
}
