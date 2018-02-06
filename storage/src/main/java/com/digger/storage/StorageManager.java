package com.digger.storage;

import com.digger.storage.model.Content;
import com.digger.storage.model.Index;
import com.digger.storage.model.Task;

import java.io.File;

/**
 * Created by xupeng on 2017/5/10.
 */
public class StorageManager {

    private TaskStorage taskStorage;
    private IndexStorage indexStorage;
    private ContentStorage contentStorage;

    private File root;

    public StorageManager(String name) {
        this.root = new File("com/digger/storage/" + name);
        if (!this.root.exists()) {
            this.root.mkdirs();
        }

    }

    public synchronized <T extends Task> TaskStorage<T> getTaskStorage(Class<T> tClass) {
        if (taskStorage == null) {
            File taskRootPath = new File(root.getAbsolutePath() + File.separatorChar + "task");
            if (!taskRootPath.exists()) {
                taskRootPath.mkdirs();
            }
            taskStorage = new TaskStorage<T>(taskRootPath, tClass);
        }
        return taskStorage;

    }

    public <I extends Index> IndexStorage<I> getIndexStorage(Class<I> tClass) {
        if (indexStorage == null) {
            File indexRootPath = new File(root.getAbsolutePath() + File.separatorChar + "index");
            if (!indexRootPath.exists()) {
                indexRootPath.mkdirs();
            }
            indexStorage = new IndexStorage<I>(indexRootPath, tClass);
        }
        return indexStorage;
    }

    public <C extends Content> ContentStorage<C> getContentStorage(Class<C> tClass) {
        if (contentStorage == null) {
            File contentRootPath = new File(root.getAbsolutePath() + File.separatorChar + "content");
            if (!contentRootPath.exists()) {
                contentRootPath.mkdirs();
            }
            contentStorage = new ContentStorage<C>(contentRootPath, tClass);
        }
        return contentStorage;

    }

    public void clear() {
        this.root.delete();
    }


}
