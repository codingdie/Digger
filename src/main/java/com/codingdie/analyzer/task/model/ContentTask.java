package com.codingdie.analyzer.task.model;

import com.codingdie.analyzer.storage.model.Index;

/**
 * Created by xupeng on 17-7-25.
 */
public abstract class ContentTask extends Task {
    abstract public <I extends Index> I getIndex();
}
