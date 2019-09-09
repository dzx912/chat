package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        Router httpRouter = Router.router(vertx);
        httpRouter.route().handler(BodyHandler.create());

        httpRouter.get("/messages").handler(this::messagesGet);
        httpRouter.post("/messages").handler(this::messagesPost);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(httpRouter::accept);
        httpServer.listen(8081);
    }

    private void messagesGet(RoutingContext request) {
        vertx.eventBus().send("db.message.history", request.getBodyAsString(), result ->
                request.response().end(result.result().body().toString())
        );
    }

    private void messagesPost(RoutingContext request) {
        vertx.eventBus().send("router", request.getBodyAsString());
        request.response().end("ok");
    }
}
