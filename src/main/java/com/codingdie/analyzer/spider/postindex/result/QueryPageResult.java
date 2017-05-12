package com.codingdie.analyzer.spider.postindex.result;

import com.codingdie.analyzer.spider.model.PostSimpleInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xupeng on 2017/4/27.
 */
public class QueryPageResult implements Serializable {
    public List<PostSimpleInfo> postSimpleInfos;
    public int  pn;
    public boolean  success=false;


}
