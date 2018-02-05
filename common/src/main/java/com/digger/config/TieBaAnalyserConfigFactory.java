package com.digger.config;

import com.digger.config.model.MasterConfig;
import com.digger.config.model.SlavesConfig;
import com.digger.config.model.SpiderConfig;
import com.digger.config.model.WorkConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xupeng on 2017/4/27.
 */
public class TieBaAnalyserConfigFactory {
    public static String configFolder = "conf";
    private static TieBaAnalyserConfigFactory tieBaAnalyserConfigFactory;
    public MasterConfig masterConfig;
    public SlavesConfig slavesConfig;
    public WorkConfig workConfig;
    public SpiderConfig spiderConfig;

    public synchronized static TieBaAnalyserConfigFactory getInstance() {
        if (tieBaAnalyserConfigFactory == null) {
            tieBaAnalyserConfigFactory = new TieBaAnalyserConfigFactory();
            Arrays.stream(tieBaAnalyserConfigFactory.getClass().getFields()).forEach(i->{
                if(!Modifier.isStatic(i.getModifiers())){
                    try {
                        String config = configFolder + File.separator + (i.getName().toLowerCase().replace("config", "")) + ".conf";

                        i.set(tieBaAnalyserConfigFactory,ConfigParser.decode(new File(config),Class.forName(i.getGenericType().getTypeName())));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

            });

        }
        return tieBaAnalyserConfigFactory;
    }

    public void saveBack() {
        Arrays.stream(tieBaAnalyserConfigFactory.getClass().getFields()).forEach(i->{
            if(!Modifier.isStatic(i.getModifiers())){
                try {
                    String configPath = configFolder + File.separator + (i.getName().toLowerCase().replace("config", "")) + ".conf";
                    File configFile= new File(configPath);
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFile,false));
                    bufferedWriter.write(ConfigParser.encode(i.get(this)));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

        });

    }





    public void updateConfig(String name, String key, String value) {
        try {
            Field field = this.getClass().getField(name);
            Object object = field.get(this);
            Field keyFieled = object.getClass().getField(key);
            if (keyFieled.getGenericType().getTypeName().equals(ConfigParser.propertyTypeStrs[0])) {
                keyFieled.set(object, value);
            } else if (keyFieled.getGenericType().getTypeName().equals(ConfigParser.propertyTypeStrs[1])) {
                keyFieled.set(object, Integer.valueOf(value).intValue());
            } else if (keyFieled.getGenericType().getTypeName().equals(ConfigParser.propertyTypeStrs[2])) {
                keyFieled.set(object, Double.valueOf(value).doubleValue());
            } else if (keyFieled.getGenericType().getTypeName().equals(ConfigParser.propertyTypeStrs[3])) {
                keyFieled.set(object, new Gson().fromJson(value,new TypeToken<List<String>>(){}.getType()));
            }
            saveBack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
