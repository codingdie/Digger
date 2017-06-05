package com.codingdie.analyzer.config;

import com.codingdie.analyzer.config.model.MasterConfig;
import com.codingdie.analyzer.config.model.SlavesConfig;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.config.model.WorkConfig;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    private final static String[] propertyTypeStrs = {
            "java.lang.String",
            "int",
            "double",
    };
    public void  updateConfig(String name,String key,String value){
        try {

            Field field= this.getClass().getField(name);
             Object object=field.get(this);
             Field keyFieled=  object.getClass().getField(key);
            System.out.println(keyFieled.getGenericType().getTypeName());
             if(keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[0])){
                 keyFieled.set(object,value);
             }else if(keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[1])){
                 keyFieled.set(object, Integer.valueOf(value).intValue());
             }else if(keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[2])){
                 keyFieled.set(object, Double.valueOf(value).doubleValue());
             }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static String getConfigFolder() {
        return configFolder;
    }

    public static void setConfigFolder(String configFolder) {
        TieBaAnalyserConfigFactory.configFolder = configFolder;
    }

    @Override
    public String toString() {
      return   new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
