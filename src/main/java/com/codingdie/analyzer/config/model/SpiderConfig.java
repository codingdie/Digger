package com.codingdie.analyzer.config.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by xupeng on 2017/4/26.
 */
public class SpiderConfig  implements Serializable{
    public String tiebaName;
    public String totalCount;
    public LocalDateTime time;
    public String cookie;

}
