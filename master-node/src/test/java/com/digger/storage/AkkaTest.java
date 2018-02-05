package com.digger.storage;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase {

    public static class A {
        private String a;

    }

    public static class B extends A {
        private String a;
    }

    public void testA() throws Exception {
        StringBuffer cmdout = new StringBuffer();
        Process process = Runtime.getRuntime().exec("telnet 127.0.0.1 90");     //执行一个系统命令
        InputStream fis = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
}
