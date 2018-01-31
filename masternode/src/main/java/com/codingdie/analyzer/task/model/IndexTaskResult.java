package com.codingdie.analyzer.task.model;

import com.codingdie.analyzer.storage.model.Index;

import java.util.List;

/**
 * Created by xupeng on 2017/7/24.
 */
public abstract class IndexTaskResult extends TaskResult {
    public abstract <T extends Index> List<T> getIndexes();
}
