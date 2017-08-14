package com.codingdie.analyzer.common.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created by xupeng on 17-8-9.
 */
public class ProcessUtil {
    public static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static CompletableFuture<Process> excute(String command, Function<String, Boolean> finishJudger) throws Exception {
        CompletableFuture<Process> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String[] cmdA = {"/bin/sh", "-c", command};
                Process process = Runtime.getRuntime().exec(cmdA);     //执行一个系统命令
                InputStream fis = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (finishJudger.apply(line)) {
                        br.close();
                        return process;
                    }
                }
                br.close();
                return process;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
        return completableFuture;
    }
}
