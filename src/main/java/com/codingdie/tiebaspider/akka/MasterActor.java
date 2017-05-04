package com.codingdie.tiebaspider.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.util.Timeout;
import com.codingdie.tiebaspider.akka.message.QueryPageTask;
import com.codingdie.tiebaspider.akka.result.QueryPageResult;
import com.codingdie.tiebaspider.config.SpiderConfigFactory;
import com.codingdie.tiebaspider.storage.SpiderWriter;
import com.google.gson.Gson;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupeng on 2017/4/26.
 */
public class MasterActor extends AbstractActor {

    private  int totalPage=0;
    private List<QueryPageTask> queryPageTasks=new ArrayList<>();
    private List<ActorRef> slaves = new ArrayList<>();
    private SpiderWriter spiderWriter;

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop MasterActor");
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        SpiderConfigFactory.getInstance().slavesConfig.hosts.iterator().forEachRemaining((String item) -> {
            String path = "akka.tcp://slave@" + item + ":2552/user/QueryPageTaskControlActor";
            ActorSelection queryPageTaskControlActor = getContext().getSystem().actorSelection(path);
            Future<ActorRef>future= queryPageTaskControlActor.resolveOne(Timeout.apply(1, TimeUnit.SECONDS));
            try {
                ActorRef actorRef=   Await.result(future, Duration.apply(1, TimeUnit.SECONDS));
                    slaves.add(actorRef);
                    System.out.println(actorRef.path().toString()+"connect succuss");

            }catch (Exception ex){
                System.out.println(path+"connect failed");
            }


        });
        System.out.println("finish connect slaves,total:"+ slaves.size());

        Integer totalCount = Integer.valueOf(SpiderConfigFactory.getInstance().targetConfig.totalCount);
        int totalPage = (totalCount-1) / 50+1;
        totalPage=30;
        for (int page = 0; page < totalPage; page++) {
            getSelf().tell(new QueryPageTask(page*50), getSelf());
        }
        spiderWriter=new SpiderWriter(SpiderConfigFactory.getInstance().targetConfig.path);
        System.out.println("totalPage:"+totalPage);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(QueryPageResult.class,r->{
            spiderWriter.write(new Gson().toJson(r));
            QueryPageTask pageTask=  queryPageTasks.stream().filter(queryPageTask -> {
                return  queryPageTask.pn==r.pn;
            }).findFirst().get();
            pageTask.finish=true;
            printProcess(pageTask);

            if(queryPageTasks.stream().allMatch(queryPageTask -> {
                return queryPageTask.finish;
            })){
                spiderWriter.flush();
                slaves.iterator().forEachRemaining(item->{
                    item.tell(QueryPageTaskControlActor.SIGN.STOP, ActorRef.noSender());
                });
               getContext().getSystem().terminate();
               System.out.println("finish all task");
            }
        }).match(QueryPageTask.class,t->{
            ActorRef queryPageTaskControlActor=null;
            while ((queryPageTaskControlActor=getSlaveToRun())==null){
                Thread.sleep(3000L);
                System.out.println("no avtive slave,wait 3 seconds to try");
            }
            queryPageTaskControlActor.tell(t, getSelf());
            queryPageTasks.add(t);
            System.out.println(queryPageTasks.size());

        }).build();
    }

    private void printProcess(QueryPageTask pageTask) {
        System.out.println("finish task "+pageTask.pn+"  "+queryPageTasks.stream().filter(i->{
            return  i.finish;
        }).count()*1.0/queryPageTasks.size()*100);
    }

    private ActorRef getSlaveToRun() {
        if(slaves.size()>0){
            return slaves.get(queryPageTasks.size() % slaves.size());
        }
        return  null;
    }
}
