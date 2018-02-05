package com.codingdie.analyzer.spider.master.tieba.model.result;

import com.codingdie.analyzer.spider.master.tieba.model.model.PostIndex;
import com.codingdie.analyzer.spider.master.tieba.model.model.PostSimpleInfo;
import com.codingdie.analyzer.task.model.IndexTaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/4/27.
 */
public class CrawlTiebaIndexResult extends IndexTaskResult {
    public List<PostSimpleInfo> postSimpleInfos = new ArrayList<>();
    public long pn;


    @Override
    public String taskId() {
        return String.valueOf(pn);
    }

    @Override
    public List<PostIndex> getIndexes() {
        return postSimpleInfos.stream().map(map -> {
            if (map.getType().equals(PostSimpleInfo.TYPE_NORMAL)) {
                return PostIndex.from(map, this);
            }else return  null;
        }).collect(Collectors.toList());
    }
}
