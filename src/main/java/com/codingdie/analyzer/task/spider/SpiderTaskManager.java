package com.codingdie.analyzer.task.spider;

import akka.actor.ActorSystem;
import com.codingdie.analyzer.storage.tieba.TieBaFileSystem;
import com.codingdie.analyzer.task.TaskManager;
import com.codingdie.analyzer.task.model.Task;

/**
 * Created by xupeng on 17-7-24.
 */
public class SpiderTaskManager<T extends Task> extends TaskManager {
    public SpiderTaskManager(Class aClass, TieBaFileSystem tieBaFileSystem, ActorSystem actorSystem, String salveActorUri) {
        super(aClass, tieBaFileSystem, actorSystem, salveActorUri);
    }
}
