package com.codingdie.analyzer.spider.model;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PageTask implements Serializable{
    public static final int STATUS_TODO=0;
    public static final int STATUS_EXCUTING=1;
    public static final int STATUS_FINISHED=2;
    public static final int STATUS_FAILED=3;

    public PageTask(int pn){
        this.pn=pn;
    }
    public long pn=50;
    public int status=STATUS_TODO;

}
