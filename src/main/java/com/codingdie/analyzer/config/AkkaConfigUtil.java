package com.codingdie.analyzer.config;



import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jsoup.helper.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaConfigUtil {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 2552;

    public static Config initAkkaConfig(String host, int port) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("" +
                "application.conf")));
        String configStr = "";
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("hostname = \"127.0.0.1\"")) {
                line = line.replace("hostname = \"127.0.0.1\"", "hostname = \"" + host + "\"");
            }
            if (line.contains("port = 2552")) {
                line = line.replace("port = 2552", "port = " + port);
            }
            configStr += line;
            configStr += "\n";
        }
        return ConfigFactory.parseString(configStr);
    }
    public static Config initAkkaConfigWithConsoleParam(String[] args) throws IOException {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 1) {
            host = args[1];
        }
        if (args.length > 2) {
            port = Integer.valueOf(args[2]) ;
        }
        return   AkkaConfigUtil.initAkkaConfig(host,port);
    }
}
