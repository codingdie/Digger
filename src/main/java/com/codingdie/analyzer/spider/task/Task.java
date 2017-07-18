package com.codingdie.analyzer.spider.task;

import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.model.PageTask;

import java.io.Serializable;

/**
 * Created by xupeng on 2017/6/12.
 */
public abstract class Task<T> implements Serializable {
    public static final int STATUS_TODO=0;
    public static final int STATUS_EXCUTING=1;
    public static final int STATUS_FINISHED=2;
    public static final int STATUS_FAILED=3;

    public Task(){
        tiebaName= TieBaAnalyserConfigFactory.getInstance().spiderConfig.tieba_name;
    }
    public int status=STATUS_TODO;
    public String cookie;
    public String tiebaName;


    public  abstract  int compareTo(T o);
    public  abstract  String getKey();

}
