package com.digger.storage.model;

/**
 * Created by xupeng on 17-7-24.
 */
public class TimeOutTaskResult extends TaskResult {
    private String taskId;

    public TimeOutTaskResult(String taskId) {
        this.taskId = taskId;
        this.errorReason(ERROR_TIMEOUT);
    }

    @Override
    public String taskId() {
        return taskId;
    }
}
