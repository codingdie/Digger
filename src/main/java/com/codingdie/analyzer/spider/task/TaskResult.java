package com.codingdie.analyzer.spider.task;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/6/12.
 */
public abstract  class TaskResult implements Serializable {
    public abstract  String getKey();
    public boolean  success=false;

}
