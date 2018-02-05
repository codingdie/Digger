package com.digger.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/27.
 */
public class SlavesConfig {
    public List<String> hosts = new ArrayList<>();
    public int page_actor_count = 1;
    public int detail_actor_count = 5;
    public int port = 2552;
    public int per_second_excute_task_count = 1;
}
