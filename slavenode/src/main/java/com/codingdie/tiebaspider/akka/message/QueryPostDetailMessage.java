package com.codingdie.tiebaspider.akka.message;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPostDetailMessage implements Serializable {
    public QueryPostDetailMessage(String postId){
        this.postId=postId;
    }
    public String postId;
}
