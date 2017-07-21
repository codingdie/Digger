package com.codingdie.analyzer.spider.model.task;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/6/12.
 */
public abstract class TaskResult implements Serializable {
    public static final String ERROR_TIMEOUT = "timeout";

    public abstract String getKey();

    public boolean success = false;
    public String error = "未知原因";

    public TaskResult() {
    }

    public <T extends TaskResult> T errorReason(String str) {
        error = str;
        success = false;
        return (T) this;
    }

    public static <T extends TaskResult> T TimeOut(Class<T> t) {
        try {
            return t.newInstance().errorReason(ERROR_TIMEOUT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
