package com.codingdie.analyzer.config;

import org.jsoup.helper.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/6/6.
 */
public class ConfigParser {
    public final static String[] propertyTypeStrs = {
            "java.lang.String",
            "int",
            "double",
            "java.util.List<java.lang.String>"
    };

    public static String encode(Object object) {
        StringBuilder stringBuilder = new StringBuilder();

        Arrays.stream(object.getClass().getFields()).forEach(field -> {
            try {
                String value="";
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (field.getGenericType().getTypeName().equals(propertyTypeStrs[0])) {
                        value=field.get(object).toString();
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[1])) {
                        value=field.get(object).toString();
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[2])) {
                        value=field.get(object).toString();
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[3])) {
                        value= StringUtil.join((Collection) field.get(object),",");
                    }
                    stringBuilder.append(field.getName() + "=" + value+ "\n");

                }
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        });

        return stringBuilder.toString();
    }

    public static <T> T decode(String str, Class<T> tClass) {
        try {
            final T t = tClass.newInstance();
            Arrays.stream(str.split("\n")).forEach(s -> {
                try {
                    String[] strs = s.split("=", 2);
                    Field field = tClass.getField(strs[0]);
                    String value = strs[1];
                    if (field.getGenericType().getTypeName().equals(propertyTypeStrs[0])) {
                        field.set(t, value);
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[1])) {
                        field.set(t, Integer.valueOf(value).intValue());
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[2])) {
                        field.set(t, Double.valueOf(value).doubleValue());
                    } else if (field.getGenericType().getTypeName().equals(propertyTypeStrs[3])) {
                        field.set(t, Arrays.stream(value.split(",")).collect(Collectors.toList()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });
            return t;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> T decode(File file, Class<T> tClass) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return decode(stringBuilder.toString(), tClass);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
