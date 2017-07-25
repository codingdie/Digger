package com.codingdie.analyzer.storage;

import com.codingdie.analyzer.spider.master.tieba.model.tieba.PostIndex;
import com.codingdie.analyzer.storage.model.Index;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by xupeng on 2017/5/10.
 */
public class IndexStorage<T extends Index> {
    Logger logger = Logger.getLogger("index");
    public static int FILE_SIZE = 10;
    private File root;
    private List<File> indexFiles = new ArrayList<>(FILE_SIZE);
    private ConcurrentHashMap<String, T> longIndexHashMap = new ConcurrentHashMap<>(200000);

    public IndexStorage(File rootPath, Class<T> tClass) {
        try {
            File file = new File(rootPath + File.separator + tClass.getSimpleName().toLowerCase());
            if (!file.exists()) {
                file.createNewFile();
            }
            this.root = file;
            for (int i = 0; i < FILE_SIZE; i++) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMaximumIntegerDigits(2);
                format.setMinimumIntegerDigits(2);
                File indexFile = new File(root.getAbsolutePath() + File.separator + (format.format(i) + ".index"));
                if (!indexFile.exists()) {
                    indexFile.createNewFile();
                }
                indexFiles.add(indexFile);
            }
            refreshAndInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshAndInit() {
        final List<File> rebuildFileList = new ArrayList<>();
        for (int i = 0; i < FILE_SIZE; i++) {
            NumberFormat format = NumberFormat.getInstance();
            format.setMaximumIntegerDigits(2);
            format.setMinimumIntegerDigits(2);
            File indexFile = new File(root.getAbsolutePath() + File.separator + (format.format(i) + "_post.index.rebuild"));
            if (!indexFile.exists()) {
                try {
                    indexFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rebuildFileList.add(indexFile);
            }
        }
        for (int i = 0; i < indexFiles.size(); i++) {
            try {
                File indexFile = indexFiles.get(i);
                File indexFileRebuld = rebuildFileList.get(i);

                BufferedReader bufferedReader = new BufferedReader(new FileReader(indexFile));
                String line = null;

                HashMap<String, T> tmpMap = new HashMap<>(2000);

                while ((line = bufferedReader.readLine()) != null) {
                    if (!StringUtil.isBlank(line)) {
                        T index = new Gson().fromJson(line, new TypeToken<T>() {
                        }.getType());

                        longIndexHashMap.put(index.getIndexId(), index);
                        tmpMap.put(index.getIndexId(), index);

                    }
                }
                final BufferedWriter todoWriter = new BufferedWriter(new FileWriter(indexFileRebuld, true));

                tmpMap.entrySet().stream().forEach(item -> {
                    try {
                        todoWriter.write(new Gson().toJson(item.getValue()));
                        todoWriter.write("\n");

                        todoWriter.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                });
                todoWriter.close();


                indexFile.delete();
                indexFileRebuld.renameTo(indexFile);
                indexFileRebuld.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }


    public void putIndex(T index) {
        if (longIndexHashMap.containsKey(index.getIndexId())) {
            logger.info("duplicate:" + new Gson().toJson(index));
        } else {
            longIndexHashMap.put(index.getIndexId(), index);
            saveIndex(index);
        }
    }

    public void modifyIndex(T index) {
        if (!longIndexHashMap.containsKey(index.getIndexId())) {
            logger.info("index not exit");
        } else {
            longIndexHashMap.put(index.getIndexId(), index);
            saveIndex(index);
        }
    }

    public int countAllIndex() {
        return longIndexHashMap.size();
    }

    public T getIndex(String index) {
        return longIndexHashMap.get(index);
    }

    public void iterateNoContentIndex(Consumer<T> function) {

        longIndexHashMap.forEachEntry(1, longPostIndexEntry -> {
            T value = longPostIndexEntry.getValue();
            if (value.getStatus() == PostIndex.STATUS_NO_CONTENT) {
                function.accept(value);
            }
        });
    }

    private void saveIndex(T index) {
        try {
            int pos = 0;
            if (StringUtil.isNumeric(index.getIndexId())) {
                pos = (int) (Long.valueOf(index.getIndexId()) % FILE_SIZE);
            } else {
                pos = (int) (index.getIndexId().hashCode() % FILE_SIZE);
            }
            File file = indexFiles.get(pos);
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(file, true));
            todoWriter.write(new Gson().toJson(index));
            todoWriter.write("\n");
            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        longIndexHashMap.clear();
    }
}
