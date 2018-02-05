package com.codingdie.digger.spider.master.tieba.model.model;

import com.codingdie.digger.spider.slave.tieba.QueryPageActor;
import com.codingdie.digger.storage.model.IndexTask;
import com.codingdie.digger.storage.model.Task;

/**
 * Created by xupeng on 2017/4/19.
 */
public class CrawlTiebaIndexTask extends IndexTask {

    public CrawlTiebaIndexTask(int pn){
        super();
        this.pn=pn;
    }
    public long pn=50;

    private String tiebaName;

    @Override
    public String taskId() {
        return String.valueOf(pn);
    }

    @Override
    public String excutorName() {
        return QueryPageActor.class.getName();
    }


    @Override
    public <T extends Task> int compareTo(T t) {
        CrawlTiebaIndexTask o1=this;
        if (t instanceof CrawlTiebaIndexTask) {
            CrawlTiebaIndexTask o2 = (CrawlTiebaIndexTask) t;
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

    public long getPn() {
        return pn;
    }

    public void setPn(long pn) {
        this.pn = pn;
    }

    public String getTiebaName() {
        return tiebaName;
    }

    public void setTiebaName(String tiebaName) {
        this.tiebaName = tiebaName;
    }
}
