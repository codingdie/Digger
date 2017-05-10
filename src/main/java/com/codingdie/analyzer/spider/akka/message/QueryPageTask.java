package com.codingdie.analyzer.spider.akka.message;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPageTask implements Serializable{
    public QueryPageTask(int pn){
        this.pn=pn;
    }
    public int pn=50;
    public boolean finish_flag =false;
    public boolean assigned_flag=false;
    public String path;

}
