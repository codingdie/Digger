package com.codingdie.analyzer.storage.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 17-7-24.
 */
public abstract class Index {
    public final static int STATUS_NO_CONTENT = 0;
    public final static int STATUS_HAS_CONTENT = 1;
    public final static int STATUS_DELETE = 2;

    private String spiderHost;
    private List<String> contentSlaves = new ArrayList<>();
    private int status = STATUS_NO_CONTENT;
    private long modifyTime = System.currentTimeMillis();
    private LocalDate date;

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getSpiderHost() {
        return spiderHost;
    }

    public void setSpiderHost(String spiderHost) {
        this.spiderHost = spiderHost;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getContentSlaves() {
        return contentSlaves;
    }

    public void setContentSlaves(List<String> contentSlaves) {
        this.contentSlaves = contentSlaves;
    }

    abstract public String getIndexId();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
