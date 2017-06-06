package com.codingdie.analyzer.config;

import com.codingdie.analyzer.config.model.MasterConfig;
import com.codingdie.analyzer.config.model.SlavesConfig;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.config.model.WorkConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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
                        System.out.println(config);

                        i.set(tieBaAnalyserConfigFactory,ConfigParser.decode(new File(config),Class.forName(i.getGenericType().getTypeName())));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

            });
        }
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(tieBaAnalyserConfigFactory));
        return tieBaAnalyserConfigFactory;
    }

    public void saveBack() {
        new JsonParser().parse(new Gson().toJson(masterConfig)).getAsJsonObject().entrySet().forEach(item -> {
            try {
                File file = new File(configFolder + File.separator + "master.conf");
                file.delete();
                file.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(item.getKey() + "=" + item.getValue().getAsString() + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    private void saveBackConfig(Object config, String fileName) {

    }

    private final static String[] propertyTypeStrs = {
            "java.lang.String",
            "int",
            "double",
    };

    public void updateConfig(String name, String key, String value) {
        try {

            Field field = this.getClass().getField(name);
            Object object = field.get(this);
            Field keyFieled = object.getClass().getField(key);
            System.out.println(keyFieled.getGenericType().getTypeName());
            if (keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[0])) {
                keyFieled.set(object, value);
            } else if (keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[1])) {
                keyFieled.set(object, Integer.valueOf(value).intValue());
            } else if (keyFieled.getGenericType().getTypeName().equals(propertyTypeStrs[2])) {
                keyFieled.set(object, Double.valueOf(value).doubleValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
