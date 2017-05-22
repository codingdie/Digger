package com.codingdie.analyzer.config;

import com.codingdie.analyzer.config.model.MasterConfig;
import com.codingdie.analyzer.config.model.SlavesConfig;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.config.model.WorkConfig;

/**
 * Created by xupeng on 2017/4/27.
 */
public class TieBaAnalyserConfigFactory {
    public static  String configFolder="conf";
    private static TieBaAnalyserConfigFactory tieBaAnalyserConfigFactory;
    public MasterConfig masterConfig;
    public SlavesConfig slavesConfig;
    public WorkConfig workConfig;
    public SpiderConfig spiderConfig;

    public synchronized static TieBaAnalyserConfigFactory getInstance() {
        if (tieBaAnalyserConfigFactory == null) {
            tieBaAnalyserConfigFactory = new TieBaAnalyserConfigFactory();
        }
        return tieBaAnalyserConfigFactory;
    }
}
