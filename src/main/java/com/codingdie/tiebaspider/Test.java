package com.codingdie.tiebaspider;

import org.apache.log4j.Logger;

/**
 * Created by xupeng on 2017/5/9.
 */
public class Test {
    public static void main(String[] args) {
         Logger netlogger =Logger.getLogger("network");
         Logger cookielogger =Logger.getLogger("cookie");
        netlogger.info("net");
        cookielogger.info("cookie");
    }
}
