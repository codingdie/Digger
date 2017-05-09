package com.codingdie.tiebaspider.config;

import java.time.LocalDateTime;

/**
 * Created by xupeng on 2017/4/26.
 */
public class TargetConfig {
    public   String tiebaName;
    public   String totalCount;
    public LocalDateTime time;
    public   String path;
    public   String cookie;
    public  int max_http_request_per_second=3;

}
