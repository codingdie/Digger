package com.codingdie.analyzer.controller;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.codingdie.analyzer.config.TieBaAnalyserConfigFactory;
import com.codingdie.analyzer.spider.postdetail.DetailSpiderMasterActor;
import com.codingdie.analyzer.spider.postindex.IndexSpiderMasterActor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

/**
 * Created by xupeng on 2017/6/2.
 */
public class MasterControllServer extends AllDirectives {


    public void start(ActorSystem actorSystem) {
        final Http http = Http.get(actorSystem);
        final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
        //In order to access all directives we need an instance where the routes are define.
        MasterControllServer app = new MasterControllServer();
        Route route1 = path(PathMatchers.segment("indexspider")
                .slash("start"), () ->
                get(() -> {
                    actorSystem.actorOf(Props.create(IndexSpiderMasterActor.class), "IndexSpiderMasterActor");

                    return complete("start indexspider");
                })
        );

        Route route2 = path(PathMatchers.segment("detailspider")
                .slash("start"), () ->
                get(() -> {
                    actorSystem.actorOf(Props.create(DetailSpiderMasterActor.class), "DetailSpiderMasterActor");
                    return complete("start detailspider");
                }));
        Route route3 = path(PathMatchers.segment("config")
                .slash("update"), () ->
                get(() -> parameter("configName",configClassName ->parameter("configJson",configJson -> {
                    JsonParser jsonParser=new JsonParser();
                    jsonParser.parse(configJson).getAsJsonObject().entrySet().forEach(jsonElementEntry -> {
                        System.out.println(jsonElementEntry.getKey()+":"+jsonElementEntry.getValue().getAsString());
                        TieBaAnalyserConfigFactory.getInstance().updateConfig(configClassName,jsonElementEntry.getKey(),jsonElementEntry.getValue().getAsString());
                    });
                    return complete("update  config succuss\n"+TieBaAnalyserConfigFactory.getInstance().toString());
                }))));
        Route route4 = path(PathMatchers.segment("config")
                .slash("show"), ()->get(() -> complete(TieBaAnalyserConfigFactory.getInstance().toString())));
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.route(route1, route2,route3,route4).flow(actorSystem, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("0.0.0.0", TieBaAnalyserConfigFactory.getInstance().masterConfig.admin_port), materializer);

    }
}
