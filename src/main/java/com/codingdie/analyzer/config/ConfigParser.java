package com.codingdie.analyzer.config;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.*;
import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Created by xupeng on 2017/6/6.
 */
public  class ConfigParser {
    public final static String[] propertyTypeStrs = {
            "java.lang.String",
            "int",
            "double",
    };
    public static  String encode(Object object){
        StringBuilder stringBuilder =new StringBuilder();
        new JsonParser().parse(new Gson().toJson(object)).getAsJsonObject().entrySet().forEach(item -> {
            try {
                stringBuilder.append(item.getKey() + "=" + item.getValue().getAsString() + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
        return stringBuilder.toString();
    }
    public static  <T> T  decode(String str,Class<T> tClass){
        try {
            final  T t= tClass.newInstance();
            Arrays.stream(str.split("\n")).forEach(s -> {
                try {
                    String[] strs= s.split("=",2);
                    Field field=tClass.getField(strs[0]);
                    String value=strs[1];
                    if (field.getGenericType().getTypeName().equals(propertyTypeStrs[0])) {
                        field.set(t, value);
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[1])) {
                        field.set(t, Integer.valueOf(value).intValue());
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[2])) {
                        field.set(t, Double.valueOf(value).doubleValue());
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            });
            return  t;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> T  decode(File file,Class<T> tClass){
        try {
            StringBuilder stringBuilder=new StringBuilder();
            BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
            String line=null;
            while ((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line+"\n");
            }
            return decode(stringBuilder.toString(),tClass);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
