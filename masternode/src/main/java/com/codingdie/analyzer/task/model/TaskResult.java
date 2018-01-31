package com.codingdie.analyzer.task.model;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/6/12.
 */
public abstract class TaskResult implements Serializable {
    public static final String ERROR_TIMEOUT = "timeout";


    public boolean success = false;
    public String error = "未知原因";
    private String taskExcuteSlave = null;

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

    public String getTaskExcuteSlave() {
        return taskExcuteSlave;
    }

    public void setTaskExcuteSlave(String taskExcuteSlave) {
        this.taskExcuteSlave = taskExcuteSlave;
    }

    public abstract String taskId();

}
