package com.digger.storage.model;


/**
 * Created by xupeng on 17-7-25.
 */
public abstract class ContentTask extends Task {
    abstract public <I extends Index> I getIndex();
}
