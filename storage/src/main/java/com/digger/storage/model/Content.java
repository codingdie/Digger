package com.digger.storage.model;

import java.time.LocalDate;

/**
 * Created by xupeng on 17-7-24.
 */
public abstract class Content {
    public abstract String getIndexId();

    public abstract LocalDate getDate();
}
