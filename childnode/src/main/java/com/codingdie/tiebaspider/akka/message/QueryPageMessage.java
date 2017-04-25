package com.codingdie.tiebaspider.akka.message;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPageMessage implements Serializable{
    public  QueryPageMessage(int pn){
        this.pn=pn;
    }
    public int pn=50;
}
