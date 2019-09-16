package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.stream.Collectors;

public class RestServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router httpRouter = Router.router(vertx);
        httpRouter.route().handler(BodyHandler.create().setUploadsDirectory("./uploads").setDeleteUploadedFilesOnEnd(true));
        httpRouter.post("/sendMessage")
                .handler(request -> {
                    vertx.eventBus().send("router", request.getBodyAsString());
                    request.response().end("ok");
                });
        httpRouter.get("/getHistory")
                .handler(request ->
                        vertx.eventBus().send("getHistory", request.getBodyAsString(), result ->
                                request.response().end(result.result().body().toString())
                        )
                );

        httpRouter.post("/uploadImage")
                .handler(request -> {
                    // handler for single file upload
                    if (request.fileUploads().size() == 1) {
                        vertx.eventBus().send("saveImage", request.fileUploads().iterator().next().uploadedFileName()
                                , result -> request.response().end(result.result().body().toString()));
                        // handler for multiple files upload
                    } else if (request.fileUploads().size() > 1) {
                        vertx.eventBus().send("saveMultipleImages", request.fileUploads().stream()
                                        .map(FileUpload::uploadedFileName)
                                        .collect(Collectors.joining(","))
                                , result -> request.response().end(result.result().body().toString()));
                    }
                });

        httpRouter.get("/getImage/:image_id").produces("image/PNG").produces("image/jpeg")
                .handler(request ->
                        vertx.eventBus().send("getImage", request.request().getParam("image_id"),
                                result -> request.response()
                                        .end(result.result().body() != null ?
                                                ((JsonObject) result.result().body()).getString("$binary")
                                                : "Image not found"))
                );

        httpServer.requestHandler(httpRouter::accept);
        httpServer.listen(8081);
    }
}
