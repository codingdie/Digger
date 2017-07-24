package com.codingdie.analyzer.storage.slave;

import com.codingdie.analyzer.storage.model.Content;
import com.codingdie.analyzer.storage.model.Index;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.time.format.DateTimeFormatter;

/**
 * Created by xupeng on 2017/5/10.
 */
public class ContentStorage<T extends Content> {

    private File root;

    public ContentStorage(File rootPath) {
        this.root = rootPath;
    }

    public boolean saveContent(T content) {
        String datePath = content.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE).replace('-', File.separatorChar);
        File file = new File(root.getAbsolutePath() + File.separator + datePath + File.separator + content.getIndexId());
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(content));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public <I extends Index> T getContent(I index) {
        String datePath = index.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE).replace('-', File.separatorChar);
        File file = new File(root.getAbsolutePath() + File.separator + datePath + File.separator + index.getIndexId());
        if (!file.exists()) {
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String all = reader.lines().reduce((s, s2) -> {
                return s + s2;
            }).get();
            reader.close();
            return new Gson().fromJson(all, new TypeToken<T>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
