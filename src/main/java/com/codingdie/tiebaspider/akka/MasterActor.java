package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.typed.javadsl.Actor;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/26.
 */
public class MasterActor extends AbstractActor {

    private  int totalPage=0;
    private List<QueryPageTask> queryPageTasks=new ArrayList<>();
    private List<ActorSelection> slaveActorSelections = new ArrayList<>();



    @Override
    public void preStart() throws Exception {
        super.preStart();
        SpiderConfigFactory.getInstance().slavesConfig.hosts.iterator().forEachRemaining(item -> {
            System.out.println("akka.tcp://slave@" + item + ":2552/user/QueryPageTaskControlActor");
            ActorSelection queryPageTaskControlActor = getContext().getSystem().actorSelection("akka.tcp://slave@" + item + ":2552/user/QueryPageTaskControlActor");
            slaveActorSelections.add(queryPageTaskControlActor);
        });

        Integer totalCount = Integer.valueOf(SpiderConfigFactory.getInstance().targetConfig.totalCount);
        int totalPage = (totalCount-1) / 50+1;
        totalPage=2;
        for (int page = 0; page < totalPage; page++) {
            getSelf().tell(new QueryPageTask(page*50), getSelf());
        }
        System.out.println("totalPage:"+totalPage);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class,r->{
            QueryPageTask pageTask=  queryPageTasks.stream().filter(queryPageTask -> {
                return  queryPageTask.pn==r.pn;
            }).findFirst().get();
            pageTask.finish=true;
            System.out.println("finish task "+pageTask.pn+"  "+queryPageTasks.stream().filter(i->{
                return  i.finish;
            }).count()*1.0/queryPageTasks.size()*100);
            if(queryPageTasks.stream().allMatch(queryPageTask -> {
                return queryPageTask.finish;
            })){
                System.out.println("finish all task ");
                slaveActorSelections.iterator().forEachRemaining(item->{
                    item.tell(QueryPageTaskControlActor.SIGN.STOP,getSelf());
                });
                getContext().getSystem().stop(getSelf());
            }
        }).match(QueryPageTask.class,t->{
            ActorSelection queryPageTaskControlActor = getSlaveToRun();
            queryPageTaskControlActor.tell(t, getSelf());
            queryPageTasks.add(t);
        }).build();
    }

    private ActorSelection getSlaveToRun() {
        return slaveActorSelections.get(queryPageTasks.size() % slaveActorSelections.size());
    }
}
