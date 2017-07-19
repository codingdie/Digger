package com.codingdie.analyzer.storage.spider;

import com.codingdie.analyzer.spider.model.PostDetail;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xupeng on 2017/5/10.
 */
public class PostDetailStorage {
    private File root;

    public PostDetailStorage(File rootPath) {
        this.root = rootPath;
    }

    public boolean savePostDetail(PostDetail detail) {
        File file = new File(root.getAbsolutePath() + File.separator + detail.getPostId());
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(new Gson().toJson(detail));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }
}
