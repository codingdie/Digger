package com.digger.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.Function;

/**
 * Created by xupeng on 17-7-31.
 */
public class FileUtil {
    public static void backwardRead(File file, Function<String, Boolean> consumer) {
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(file, "r");
            long len = rf.length();
            long start = rf.getFilePointer();
            long nextend = start + len - 1;
            if (nextend < 0) return;
            String line;
            rf.seek(nextend);
            int c = -1;
            while (nextend > start) {
                c = rf.read();
                if (c == '\n' || c == '\r') {
                    String s = rf.readLine();
                    if (s != null) {
                        line = new String(s.getBytes("ISO-8859-1"), "utf-8");
                        if (!consumer.apply(line)) break;
                    }
                    nextend--;
                }
                nextend--;
                rf.seek(nextend);
                if (nextend == 0) {
                    line = new String(rf.readLine().getBytes("ISO-8859-1"), "utf-8");
                    consumer.apply(line);
                }
            }
        } catch (Exception e) {
            System.out.println(file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
