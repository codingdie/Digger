package com.codingdie.tiebaspider.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xupeng on 2017/5/2.
 */
public class SpiderWriter {
    private File file;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    public SpiderWriter(String path) {
        file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void write(String s) {
        try {
            bufferedWriter.write(s);
            bufferedWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void flush() {
        try {
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
