package com.codingdie.tiebaspider.config;

/**
 * Created by xupeng on 2017/4/27.
 */
public class SpiderConfigFactory {
    private  static SpiderConfigFactory spiderConfigFactory;
    public  MasterConfig masterConfig;
    public  SlavesConfig slavesConfig;
    public  TargetConfig targetConfig;

    public  synchronized static SpiderConfigFactory getInstance(){
        if(spiderConfigFactory ==null){
            spiderConfigFactory =new SpiderConfigFactory();
        }
        return spiderConfigFactory;
    }
}
