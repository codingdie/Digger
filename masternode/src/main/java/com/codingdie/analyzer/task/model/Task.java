package com.codingdie.analyzer.task.model;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/6/12.
 */
public abstract class Task implements Serializable {
    public static final int STATUS_TODO = 0;
    public static final int STATUS_EXCUTING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_FAILED = 3;
    public int status = STATUS_TODO;
    public String cookie;

    public abstract String taskId();

    public abstract String excutorName();

    public <T extends Task> int compareTo(T t) {
        return this.status - t.status;
    }


}
