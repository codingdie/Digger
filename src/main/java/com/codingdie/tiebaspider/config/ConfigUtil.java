package com.codingdie.tiebaspider.config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigUtil {
    public static void initConfig(String[] args) throws IOException {
        SpiderConfigFactory.getInstance().masterConfig= ConfigUtil.initMasterConfig(args);
        SpiderConfigFactory.getInstance().targetConfig=ConfigUtil.initTargetConfig(args);
        SpiderConfigFactory.getInstance().slavesConfig=ConfigUtil.initSlavesConfig(args);
    }
    public static SlavesConfig initSlavesConfig(String[] args) {
        List<String> configPaths = new ArrayList<String>();
        if (args.length > 0) {
            configPaths.add(args[0]);
        }
        configPaths.add("conf/slaves.conf");
        configPaths.add("slaves.conf");

        return parseSlavesConfig(configPaths);
    }
    public static TargetConfig initTargetConfig(String[] args) throws IOException {
        List<String> configPaths = new ArrayList<String>();
        if (args.length > 1) {
            configPaths.add(args[1]);
        }
        configPaths.add("conf/spider.conf");
        configPaths.add("spider.conf");

        TargetConfig targetConfig= parseTargetConfig(configPaths);

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url("http://tieba.baidu.com/f?kw=justice_eternal&ie=utf-8")
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.out.println(("Unexpected code " + response));
        } else {
            String string = response.body().string();
            Document document = Jsoup.parse(string);
            targetConfig.totalCount =document.select(".last.pagination-item").get(0).attr("href").split("pn=")[1];
            targetConfig.time= LocalDateTime.now();
        }
        client=null;
        return  targetConfig;
    }

    public static MasterConfig initMasterConfig(String[] args) throws IOException {
        List<String> configPaths = new ArrayList<String>();
        if (args.length > 1) {
            configPaths.add(args[1]);
        }
        configPaths.add("conf/master.conf");
        configPaths.add("master.conf");
        return  parseMasterConfig(configPaths);
    }

    private static SlavesConfig parseSlavesConfig(List<String> confPaths) {
        SlavesConfig slavesConfig=new SlavesConfig();

        try {
            String confPath = confPaths.stream().filter(s -> {
                if (new File(s).exists()) {
                    System.out.println(new File(s).getAbsolutePath());

                    return true;
                }
                return false;
            }).findFirst().orElse(null);
            if (confPath != null) {
                File file = new File(confPath);
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.contains("hosts=")){
                        String hostsstr=line.replace("hosts=","").trim();
                        Arrays.stream(hostsstr.split(";")).iterator().forEachRemaining(item->{
                            slavesConfig.hosts.add(item);
                        });
                    }
                    if(line.contains("detail_actor_count=")){
                        slavesConfig.detail_actor_count=Integer.valueOf(line.replace("detail_actor_count=","").trim());
                    }
                    if(line.contains("page_actor_count=")){
                        slavesConfig.page_actor_count=Integer.valueOf(line.replace("page_actor_count=","").trim());
                    }
                }
                bufferedReader.close();
                fileReader.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return slavesConfig;


    }

    private static TargetConfig parseTargetConfig(List<String> confPaths) {
        TargetConfig targetConfig=null;
        try {
            String confPath = confPaths.stream().filter(s -> {
                if (new File(s).exists()) {
                    System.out.println(new File(s).getAbsolutePath());

                    return true;
                }
                return false;
            }).findFirst().orElse(null);
            if (confPath != null) {
                File file = new File(confPath);
                FileReader fileReader = new FileReader(file);

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                String total = "";

                while ((line = bufferedReader.readLine()) != null) {
                    if(line.contains("tieba_name=")){
                        targetConfig=new TargetConfig();
                        targetConfig.tiebaName=line.replace("tieba_name=","").trim();
                    }
                }
                bufferedReader.close();
                fileReader.close();


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return targetConfig;


    }

    private static MasterConfig parseMasterConfig(List<String> confPaths) {
        MasterConfig masterConfig=null;
        try {
            String confPath = confPaths.stream().filter(s -> {
                if (new File(s).exists()) {
                    System.out.println(new File(s).getAbsolutePath());

                    return true;
                }
                return false;
            }).findFirst().orElse(null);
            if (confPath != null) {
                File file = new File(confPath);
                FileReader fileReader = new FileReader(file);

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                String total = "";

                while ((line = bufferedReader.readLine()) != null) {
                    if(line.contains("host=")){
                        masterConfig=new MasterConfig();
                        masterConfig.host=line.replace("host=","").trim();
                    }
                }
                bufferedReader.close();
                fileReader.close();


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return masterConfig;


    }


}