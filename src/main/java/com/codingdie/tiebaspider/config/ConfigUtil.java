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

    public static final String SPILLTER = ",";

    public static void initConfig(String configFolder) throws IOException {
        SpiderConfigFactory.getInstance().masterConfig= ConfigUtil.initMasterConfig(configFolder);
        SpiderConfigFactory.getInstance().targetConfig=ConfigUtil.initTargetConfig(configFolder);
        SpiderConfigFactory.getInstance().slavesConfig=ConfigUtil.initSlavesConfig(configFolder);
    }
    public static SlavesConfig initSlavesConfig(String configFolder) {
        List<String> configPaths = getConfigPaths(configFolder,"slaves.conf");

        return parseSlavesConfig(configPaths);
    }

    private static List<String> getConfigPaths(String configFolder,String filename) {
        List<String> configPaths = new ArrayList<String>();
        if (configFolder!=null&&configFolder.length()>0) {
            configPaths.add(configFolder+"/"+filename);
        }
        configPaths.add("../conf/"+filename);
        configPaths.add(filename);
        configPaths.add("conf/"+filename);
        return configPaths;
    }

    public static TargetConfig initTargetConfig(String configFolder) throws IOException {
        List<String> configPaths = getConfigPaths(configFolder,"spider.conf");

        TargetConfig targetConfig= parseTargetConfig(configPaths);

        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).readTimeout(60, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url("http://tieba.baidu.com/f?kw=justice_eternal&ie=utf-8")
//                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.110 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.out.println(("Unexpected code " + response));
        } else {
            if(response.isSuccessful()){
            String string = response.body().string();
                System.out.println(string);
            Document document = Jsoup.parse(string);
            targetConfig.totalCount =document.select(".last.pagination-item").get(0).attr("href").split("pn=")[1];
            targetConfig.time= LocalDateTime.now();
            }
        }
        client=null;
        return  targetConfig;
    }

    public static MasterConfig initMasterConfig(String configFolder) throws IOException {
        List<String> configPaths = getConfigPaths(configFolder,"master.conf");

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
                        Arrays.stream(hostsstr.split(SPILLTER)).iterator().forEachRemaining(item->{
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
                        if(targetConfig==null){
                            targetConfig=new TargetConfig();
                        }
                        targetConfig.tiebaName=line.replace("tieba_name=","").trim();
                    }
                    if(line.contains("path=")){
                        if(targetConfig==null){
                            targetConfig=new TargetConfig();
                        }
                        targetConfig.path=line.replace("path=","").trim();
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