package com.codingdie.analyzer.config;

/**
 * Created by xupeng on 2017/4/27.
 */
public class SpiderConfigFactory {
    private static SpiderConfigFactory spiderConfigFactory;
    public MasterConfig masterConfig;
    public SlavesConfig slavesConfig;
    public WorkConfig workConfig;

    public synchronized static SpiderConfigFactory getInstance() {
        if (spiderConfigFactory == null) {
            spiderConfigFactory = new SpiderConfigFactory();
        }
        return spiderConfigFactory;
    }
}
