package com.codingdie.analyzer.spider.model;

import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.spider.task.Task;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PageTask extends Task<PageTask>  {

    public PageTask(int pn){
        super();
        this.pn=pn;
    }
    public long pn=50;

    @Override
    public int compareTo(PageTask o) {
        PageTask o1=this;
        PageTask o2=o;

        if(o1.pn>o2.pn){
            return  1;
        }else{
            if(o1.pn==o2.pn){
                return o1.status-o2.status;

            }else{
                return -1;
            }
        }
    }

    @Override
    public String getKey() {
        return String.valueOf(pn);
    }
}
