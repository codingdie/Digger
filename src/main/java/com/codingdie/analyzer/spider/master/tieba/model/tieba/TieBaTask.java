package com.codingdie.analyzer.spider.master.tieba.model.tieba;

import com.codingdie.analyzer.task.model.Task;

/**
 * Created by xupeng on 17-7-24.
 */
public abstract class TieBaTask extends Task {
    private String tiebaName;

    public String getTiebaName() {
        return tiebaName;
    }

    public void setTiebaName(String tiebaName) {
        this.tiebaName = tiebaName;
    }
}
