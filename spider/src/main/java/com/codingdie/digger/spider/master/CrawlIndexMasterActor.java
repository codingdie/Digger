package com.codingdie.digger.spider.master;

import akka.actor.AbstractActor;
import com.codingdie.digger.spider.network.HttpService;
import com.codingdie.analyzer.task.TaskManager;
import com.codingdie.digger.storage.StorageManager;
import com.codingdie.digger.storage.TaskStorage;
import com.codingdie.digger.storage.model.Index;
import com.codingdie.digger.storage.model.IndexTask;
import com.codingdie.digger.storage.model.IndexTaskResult;

import java.util.List;

/**
 * Created by xupeng on 2017/4/26.
 */
public abstract class CrawlIndexMasterActor<I extends Index, T extends IndexTask, R extends IndexTaskResult> extends AbstractActor {


    private TaskManager<T> taskManager;
    private StorageManager storageManager;

    @Override
    public void postStop() throws Exception {
        taskManager.stopManager();
        super.postStop();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        HttpService.getInstance().destroy();
        startTaskManager();
    }


    private void startTaskManager() {
        System.out.println("开始初始化IndexTask");
        storageManager = new StorageManager(getStorageName());
        TaskStorage<T> taskStorage = storageManager.getTaskStorage(getIndexTaskClass());
        taskManager = new TaskManager<T>(getIndexTaskClass(), taskStorage, getContext().getSystem());
        if (taskManager.getTotalTaskSize() == 0) {
            initIndexTask().forEach(t -> {
                taskManager.putTask(t);
            });
        }
        System.out.println("开始分发执行IndexTask");
        taskManager.startAlloc(getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(getIndexTaskResultClass(), r -> {
            if (r.success) {
                r.getIndexes().forEach(item -> {
                    I index = (I) item;
                    storageManager.getIndexStorage(getIndexClass()).putIndex(index);
                    onGetIndex(index);
                });
            }
            taskManager.receiveResult(r, getSender());
        }).build();
    }

    public abstract String getStorageName();

    public abstract Class<I> getIndexClass();

    public abstract Class<T> getIndexTaskClass();

    public abstract Class<R> getIndexTaskResultClass();

    public abstract List<T> initIndexTask();

    public abstract void onGetIndex(I index);


}
