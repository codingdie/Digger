package com.codingdie.tiebaspider;

import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/5/5.
 */
public class HttpUtil {
    private static String proxyServer = "proxy.abuyun.com";
    private static  int proxyPort      = 9020;

    public static void newCookie(List<String>  setCookies) {
        setCookies.iterator().forEachRemaining(str->{
            String[] strs=  str.split(";")[0].split(";");
            System.out.println(strs[0]+"="+strs[1]);
            String newCokkie = Arrays.stream(SpiderConfigFactory.getInstance().targetConfig.cookie.split(";")).filter(s -> {
                return !s.contains(strs[0]);
            }).reduce(new BinaryOperator<String>() {
                @Override
                public String apply(String s, String s2) {

                    return s + ";" + s2;
                }
            }).get() + ";" + strs[0]+"="+strs[1];
            SpiderConfigFactory.getInstance().targetConfig.cookie= newCokkie;
        });
       
    }


    public  static  OkHttpClient buildClient() {
        return new OkHttpClient.Builder().followRedirects(false).proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer, proxyPort))).readTimeout(10, TimeUnit.SECONDS).build();
    }

}
