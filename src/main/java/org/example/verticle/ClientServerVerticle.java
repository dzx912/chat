package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class ClientServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();

        Router httpRouter = Router.router(vertx);

        httpRouter.route("/*")
                .handler(StaticHandler.create()
                        .setCachingEnabled(false)
                        .setWebRoot("static")
                );
        httpServer.requestHandler(httpRouter::accept);

        httpServer.listen(8082);
    }
}
