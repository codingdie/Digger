package com.codingdie.analyzer.config;

import com.codingdie.analyzer.config.model.MasterConfig;
import com.codingdie.analyzer.config.model.SlavesConfig;
import com.codingdie.analyzer.config.model.SpiderConfig;
import com.codingdie.analyzer.config.model.WorkConfig;
import com.codingdie.analyzer.spider.network.HttpService;
import okhttp3.Request;
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

public class ConfigUtil {

    public static final String SPILLTER = ",";

    public static void initConfigForMaster(String configFolder) {
        try {
            System.out.println(configFolder);
            TieBaAnalyserConfigFactory.getInstance().masterConfig = ConfigUtil.initMasterConfig(configFolder);
            TieBaAnalyserConfigFactory.getInstance().slavesConfig = ConfigUtil.initSlavesConfig(configFolder);
            TieBaAnalyserConfigFactory.getInstance().workConfig = ConfigUtil.initWorkConfig(configFolder);
            TieBaAnalyserConfigFactory.getInstance().spiderConfig = ConfigUtil.initSpiderConfig(configFolder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void initConfigForSlave(String configFolder) {
        try {
            System.out.println(configFolder);
            TieBaAnalyserConfigFactory.getInstance().slavesConfig = ConfigUtil.initSlavesConfig(configFolder);
            TieBaAnalyserConfigFactory.getInstance().workConfig = ConfigUtil.initWorkConfig(configFolder);
            TieBaAnalyserConfigFactory.getInstance().spiderConfig = ConfigUtil.initSpiderConfig(configFolder);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static SlavesConfig initSlavesConfig(String configFolder) {
        List<String> configPaths = getConfigPaths(configFolder, "slaves.conf");

        return parseSlavesConfig(configPaths);
    }

    private static List<String> getConfigPaths(String configFolder, String filename) {
        List<String> configPaths = new ArrayList<String>();
        if (configFolder != null && configFolder.length() > 0) {
            configPaths.add(configFolder + "/" + filename);
        }
        configPaths.add("../conf/" + filename);
        configPaths.add(filename);
        configPaths.add("conf/" + filename);
        return configPaths;
    }

    public static SpiderConfig initSpiderConfig(String configFolder) throws IOException {
        List<String> configPaths = getConfigPaths(configFolder, "spider.conf");

        SpiderConfig spiderConfig = parseSpiderTargetConfig(configPaths);
        TieBaAnalyserConfigFactory.getInstance().spiderConfig = spiderConfig;


        System.out.println(spiderConfig.tiebaName);
        String string = HttpService.getInstance().excute(new Request.Builder()
                .url("https://tieba.baidu.com/f?kw=" + spiderConfig.tiebaName + "&ie=utf-8").build(),null);
        Document document = Jsoup.parse(string);
        spiderConfig.totalCount = document.select(".last.pagination-item").get(0).attr("href").split("pn=")[1];
        spiderConfig.time = LocalDateTime.now();


        return spiderConfig;
    }
    public static WorkConfig initWorkConfig(String configFolder) throws IOException {
        List<String> configPaths = getConfigPaths(configFolder, "work.conf");

        return parseWorkConfig(configPaths);
    }

    public static MasterConfig initMasterConfig(String configFolder) throws IOException {
        List<String> configPaths = getConfigPaths(configFolder, "master.conf");

        return parseMasterConfig(configPaths);
    }

    private static SlavesConfig parseSlavesConfig(List<String> confPaths) {
        SlavesConfig slavesConfig = new SlavesConfig();

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
                    if (line.contains("hosts=")) {
                        String hostsstr = line.replace("hosts=", "").trim();
                        Arrays.stream(hostsstr.split(SPILLTER)).iterator().forEachRemaining(item -> {
                            slavesConfig.hosts.add(item);
                        });
                    }
                    if (line.contains("detail_actor_count=")) {
                        slavesConfig.detail_actor_count = Integer.valueOf(line.replace("detail_actor_count=", "").trim());
                    }
                    if (line.contains("page_actor_count=")) {
                        slavesConfig.page_actor_count = Integer.valueOf(line.replace("page_actor_count=", "").trim());
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

    private static SpiderConfig parseSpiderTargetConfig(List<String> confPaths) {
        SpiderConfig spiderConfig = null;
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
                    if (line.contains("tieba_name=")) {
                        if (spiderConfig == null) {
                            spiderConfig = new SpiderConfig();
                        }
                        spiderConfig.tiebaName = line.replace("tieba_name=", "").trim();
                    }

                    if (line.contains("cookie=")) {
                        if (spiderConfig == null) {
                            spiderConfig = new SpiderConfig();
                        }
                        spiderConfig.cookie = line.replace("cookie=", "").trim();
                    }

                }
                bufferedReader.close();
                fileReader.close();


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return spiderConfig;


    }

    private static WorkConfig parseWorkConfig(List<String> confPaths) {
        WorkConfig workConfig = null;
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
                    if (line.contains("max_http_request_per_second=")) {
                        if(workConfig==null){
                            workConfig = new WorkConfig();
                        }
                        workConfig.max_http_request_per_second = Integer.valueOf(line.replace("max_http_request_per_second=", "").trim());
                    }
                }
                bufferedReader.close();
                fileReader.close();


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return workConfig;


    }

    private static MasterConfig parseMasterConfig(List<String> confPaths) {
        MasterConfig masterConfig = null;
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
                    if (line.contains("host=")) {
                        masterConfig = new MasterConfig();
                        masterConfig.host = line.replace("host=", "").trim();
                    }
                    if (line.contains("max_running_task=")) {
                        if(masterConfig==null){
                            masterConfig = new MasterConfig();
                        }

                        masterConfig.max_running_task = Integer.valueOf(line.replace("max_running_task=", "").trim());
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