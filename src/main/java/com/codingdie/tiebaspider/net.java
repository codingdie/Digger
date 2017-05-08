package com.codingdie.tiebaspider;

import akka.dispatch.ExecutorServiceFactory;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by xupeng on 2017/5/5.
 */
public class net {
    public static OkHttpClient client = buildClient();
    static   int i=0;

    private static OkHttpClient buildClient() {
        return new OkHttpClient.Builder().followRedirects(false).readTimeout(10, TimeUnit.SECONDS).build();
    }

    public static void main(String[] args) throws  Exception{
        Executor executor= Executors.newFixedThreadPool(5);
        while (true){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        long begin=System.currentTimeMillis();
                        int m=0;
                        while (true){
                            m++;
                            String query = query(i);

                            if(query !=null){
                                break;
                            }
                        }
                        System.out.println(m+"次尝试;"+i+":"+(System.currentTimeMillis()-begin));
                        i+=50;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }
            });

        }


    }


    private static String query(int i) throws Exception {
        String url = "http://tieba.baidu.com/f?kw=李毅&ie=utf-8&pn=" +i;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        QueryPageResult queryPageResult=new QueryPageResult();
        queryPageResult.pn=i;

        if (!response.isSuccessful()) {
            if(response.code()==302){
                response.headers().names().iterator().forEachRemaining(item->{
                    System.out.println(item+":"+ response.header(item));

                });
            }
            Thread.sleep(3000L);
             return null;
        } else {
            return response.body().string();

        }
    }
}
