package com.codingdie.analyzer.spider.model.tieba;

import com.codingdie.analyzer.task.model.Task;

/**
 * Created by xupeng on 2017/4/19.
 */
public class PageTask extends TieBaTask {

    public PageTask(int pn){
        super();
        this.pn=pn;
    }
    public long pn=50;


    @Override
    public String taskId() {
        return String.valueOf(pn);
    }

    @Override
    public String excutorName() {
        return null;
    }

    @Override
    public <T extends Task> int compareTo(T t) {
        PageTask o1=this;
        if (t instanceof PageTask) {
            PageTask o2 = (PageTask) t;
            if (o1.pn > o2.pn) {
                return 1;
            } else {
                if (o1.pn == o2.pn) {
                    return o1.status - o2.status;

                } else {
                    return -1;
                }
            }
        } else {
            return o1.status - t.status;
        }
    }


}
