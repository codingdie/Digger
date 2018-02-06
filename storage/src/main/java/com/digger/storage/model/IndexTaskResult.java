package com.digger.storage.model;


import java.util.List;

/**
 * Created by xupeng on 2017/7/24.
 */
public abstract class IndexTaskResult extends TaskResult {
    public abstract <T extends Index> List<T> getIndexes();
}
