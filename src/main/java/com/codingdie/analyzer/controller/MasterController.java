package com.codingdie.analyzer.controller;
import static akka.pattern.PatternsCS.gracefulStop;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.pattern.*;
import com.codingdie.analyzer.spider.postdetail.DetailSpiderMasterActor;
import com.codingdie.analyzer.spider.postindex.IndexSpiderMasterActor;
import com.codingdie.analyzer.spider.postindex.QueryPageActor;
import scala.compat.java8.functionConverterImpls.FromJavaConsumer;

/**
 * Created by xupeng on 2017/5/16.
 */
public class MasterController extends AbstractActor {
    public  static enum SIGN {INDEX_SPIDER,DETAIL_SPIDER};

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("stop MasterController");
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

    }

    @Override
    public Receive createReceive() {

        return receiveBuilder().matchEquals(SIGN.INDEX_SPIDER,i->{

             getContext().getSystem().actorOf(Props.create(IndexSpiderMasterActor.class), "IndexSpiderMasterActor");
        }).matchEquals(SIGN.DETAIL_SPIDER,i->{
            getContext().getSystem().actorOf(Props.create(DetailSpiderMasterActor.class), "DetailSpiderMasterActor");

        }).match(String.class,s->{
            System.out.println("xupeng:"+s);
        }).build();
    }
}
