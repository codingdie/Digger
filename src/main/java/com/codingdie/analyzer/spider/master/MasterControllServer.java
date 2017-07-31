package com.codingdie.analyzer.spider.master;

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
import com.codingdie.analyzer.spider.master.tieba.CrawlTiebaContentMasterActor;
import com.codingdie.analyzer.spider.master.tieba.CrawlTiebaIndexMasterActor;

import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

/**
 * Created by xupeng on 2017/6/2.
 */
public class MasterControllServer extends AllDirectives {


    public void start(ActorSystem actorSystem) {
        final Http http = Http.get(actorSystem);
        final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
        //In order to access all directives we need an instance where the routes are define.
        MasterControllServer app = new MasterControllServer();
        Route route0 = path("", () ->
                get(() -> getFromResource("web/index.html"))
        );

//        Route route1 = path(PathMatchers.segment("analyser")
//                .slash("create"), () ->
//                get(() -> {
//                    actorSystem.actorOf(Props.create(CrawlTiebaIndexMasterActor.class), "CrawlIndexMasterActor");
//                    return complete("start indexspider");
//                })
//        );
        Route route1 = path(PathMatchers.segment("indexspider")
                .slash("start"), () ->
                get(() -> parameter("tiebaName", tiebaName -> {
                    actorSystem.actorOf(Props.create(CrawlTiebaIndexMasterActor.class, tiebaName), "CrawlIndexMasterActor");
                    return complete("start indexspider");
                })));
        Route route2 = path(PathMatchers.segment("detailspider")
                .slash("start"), () ->
                get(() -> {
                    actorSystem.actorOf(Props.create(CrawlTiebaContentMasterActor.class), "CrawlTiebaContentMasterActor");
                    return complete("start detailspider");
                }));
        Route route3 = path(PathMatchers.segment("config")
                .slash("update"), () ->
                get(() -> parameter("configName", configName -> parameter("configField", configField -> parameter("configValue", configValue -> {
                    TieBaAnalyserConfigFactory.getInstance().updateConfig(configName, configField, configValue);
                    return complete("update  config succuss");
                })))));
        Route route4 = path(PathMatchers.segment("config")
                .slash("show"), () -> get(() -> complete(TieBaAnalyserConfigFactory.getInstance().toString())));
        Route route5 = path("logs", () -> get(() -> getFromBrowseableDirectory("logs")));
        Route route6 = path(PathMatchers.segment("logs").slash(PathMatchers.segment(Pattern.compile(".*.log"))), s -> complete("123"));
        Route route7 = path(PathMatchers.segment("index")
                .slash("count"), () ->
                get(() -> parameter("tiebaName", tiebaName -> {
                    return complete("");
                    //complete(String.valueOf(TieBaFileSystem.getInstance(tiebaName, TieBaFileSystem.ROLE_MASTER).getIndexStorage().countAllIndex()));
                }))
        );
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.route(route0, route1, route2, route3, route4, route5, route6, route7).flow(actorSystem, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("0.0.0.0", TieBaAnalyserConfigFactory.getInstance().masterConfig.admin_port), materializer);

    }


}
