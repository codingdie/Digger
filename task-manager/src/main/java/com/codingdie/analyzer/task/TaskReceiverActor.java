package com.codingdie.analyzer.task;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.BalancingPool;
import com.codingdie.digger.storage.model.Task;
import com.codingdie.digger.storage.model.TimeOutTaskResult;

import java.util.concurrent.ConcurrentHashMap;

import static akka.pattern.PatternsCS.ask;

/**
 * Created by xupeng on 17-7-24.
 */
public class TaskReceiverActor extends AbstractActor {

    ConcurrentHashMap<String, ActorRef> excutorMap = new ConcurrentHashMap<>();

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(t -> {
            if (t instanceof Task) {
                Task task = (Task) t;
                ActorRef router = null;
                if (excutorMap.containsKey(task.excutorName())) {
                    router = excutorMap.get(task.excutorName());
                } else {
                    router = context().actorOf(Props.create(Class.forName(task.excutorName())).withRouter(new BalancingPool(10)));
                    excutorMap.put(task.excutorName(), router);
                }
                final ActorRef sender = sender();
                final ActorRef self = getSelf();
                ask(router, task, 500000).whenComplete((result, throwable) -> {
                    if (result == null) {
                        sender.tell(new TimeOutTaskResult(task.taskId()), self);
                    } else {
                        sender.tell(result, self);
                    }
                });
            }

        }).build();
    }
}
