package com.digger.storage;

import com.digger.storage.model.Index;
import com.digger.util.FileUtil;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Created by xupeng on 2017/5/10.
 */
public class IndexStorage<T extends Index> {
    public static int FILE_SIZE = 10;
    private static ConcurrentHashMap<String, Object> indexCachePool = new ConcurrentHashMap<>(200000);
    Logger logger = Logger.getLogger("index");
    private File root;
    private Class<T> tclass;
    private List<File> indexFiles = new ArrayList<>(FILE_SIZE);

    public IndexStorage(File rootPath, Class<T> tClass) {
        try {
            tclass = tClass;
            File file = new File(rootPath + File.separator + tClass.getSimpleName().toLowerCase());
            if (!file.exists()) {
                file.mkdirs();
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
            }
            rebuildFileList.add(indexFile);
        }
        for (int i = 0; i < indexFiles.size(); i++) {
            try {
                File indexFile = indexFiles.get(i);
                File indexFileRebuld = rebuildFileList.get(i);
                long begin = System.currentTimeMillis();
                rebuildFile(indexFile, indexFileRebuld);
                System.out.println("total:" + (System.currentTimeMillis() - begin));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void rebuildFile(File indexFile, File indexFileRebuld) throws IOException {
        Gson gson = new Gson();
        final BufferedWriter todoWriter = new BufferedWriter(new FileWriter(indexFileRebuld, true));
        AtomicInteger i = new AtomicInteger();

        FileUtil.backwardRead(indexFile, line -> {
            T index = gson.fromJson(line, tclass);
            try {
                String str = gson.toJson(index);
                todoWriter.write(str);
                todoWriter.write("\n");
                i.incrementAndGet();
                if (i.get() % 100 == 0) {
                    todoWriter.flush();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        });
        todoWriter.flush();
        todoWriter.close();
        indexFile.delete();
        indexFileRebuld.renameTo(indexFile);
        indexFileRebuld.delete();

    }


    public void putIndex(T index) {
        saveIndex(index);
    }

    public int countAllIndex() {
        Set<String> indexIdList = new HashSet<>();
        indexFiles.stream().forEach(file -> {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line.isEmpty();
                    indexIdList.add("!23");
                }
                bufferedReader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return indexIdList.size();
    }

    public T getIndex(String index) {
        Object obj = null;
        if (obj != null) {
            return (T) obj;
        } else {
            return getIndexFromFile(index);
        }
    }

    private T getIndexFromFile(String index) {
        File file = getFile(index);
        AtomicReference<T> reference = new AtomicReference<>();
        FileUtil.backwardRead(file, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                T t = new Gson().fromJson(s, tclass);
                if (t.getIndexId().equals(index)) {
                    reference.getAndSet(t);
                    return false;
                }
                return true;
            }
        });
        return reference.get();
    }

    private void saveIndex(T index) {
        try {
            File file = getFile(index.getIndexId());
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(file, true));
            todoWriter.write(new Gson().toJson(index));
            todoWriter.write("\n");
            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile(String indexId) {
        int pos = 0;
        if (StringUtil.isNumeric(indexId)) {
            pos = (int) (Long.valueOf(indexId) % FILE_SIZE);
        } else {
            pos = (int) (indexId.hashCode() % FILE_SIZE);
        }
        return indexFiles.get(pos);
    }

}
