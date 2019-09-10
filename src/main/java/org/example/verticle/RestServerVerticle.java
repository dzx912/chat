package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import static org.example.responses.Responses.error;

public class RestServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        Router httpRouter = Router.router(vertx);
        httpRouter.route().handler(BodyHandler.create());

        httpRouter.get("/messages").handler(this::messagesGet);
        httpRouter.post("/messages").handler(this::messagesPost);

        httpRouter.getWithRegex("/images$").handler(this::imagesGet);
        httpRouter.getWithRegex("/images/(?<imageId>[^/]+)$").handler(this::imagesGetById);
        httpRouter.postWithRegex("/images$").blockingHandler(this::imagesPostBlocking);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(httpRouter::accept);
        httpServer.listen(8081);
    }

    private void messagesGet(RoutingContext route) {
        vertx.eventBus().send("db.message.history", route.getBodyAsString(), (AsyncResult<Message<String>> result) ->
                route.response().end(result.result().body())
        );
    }

    private void messagesPost(RoutingContext route) {
        vertx.eventBus().send("router", route.getBodyAsString());
        route.response().end("ok");
    }

    private void imagesGet(RoutingContext route) {
        vertx.eventBus().send("db.images.get", null, (AsyncResult<Message<JsonObject>> async) -> {
            JsonObject result = async.result().body();
            route.response()
                    .setStatusCode(result.containsKey("error") ? 500 : 200)
                    .putHeader("Content-Type", "application/json")
                    .end(result.encodePrettily());
        });
    }

    private void imagesGetById(RoutingContext route) {
        String imageId = route.pathParam("imageId");

        vertx.eventBus().send("db.images.getById", imageId, (AsyncResult<Message<JsonObject>> asyncFind) -> {
            JsonObject result = asyncFind.result().body();

            if (result.containsKey("error")) {
                route.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(result.encodePrettily());
                return;
            }

            JsonObject imageFile = result.getJsonObject("result");

            if (imageFile == null) {
                route.response()
                        .setStatusCode(404)
                        .putHeader("Content-Type", "application/json")
                        .end(error("Image Id not found: " + imageId).encodePrettily());
                return;
            }

            vertx.fileSystem().open(imageFile.getString("uploadedFileName"), new OpenOptions(), asyncRead -> {
                if (asyncRead.failed()) {
                    route.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end(error(asyncRead.cause()).encodePrettily());
                    return;
                }

                String disposition = "inline; filename*=UTF-8''" + encode(imageFile.getString("originalFileName"));

                HttpServerResponse response = route.response()
                        .putHeader("Content-Type", imageFile.getString("contentType"))
                        .putHeader("Content-Disposition", disposition);

                response.setChunked(true);

                AsyncFile asyncFile = asyncRead.result();
                asyncFile.endHandler(ignored -> {
                    asyncFile.close();
                    response.end();
                });

                Pump.pump(asyncFile, response).start();
            });
        });
    }

    private void imagesPostBlocking(RoutingContext route) {
        FileSystem fileSystem = vertx.fileSystem();

        Set<FileUpload> fileUploads = route.fileUploads();

        if (fileUploads.size() != 1) {
            fileUploads.stream().map(FileUpload::uploadedFileName).forEach(fileSystem::deleteBlocking);
            route.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(error("Multiple file uploads in one request are not supported").encodePrettily());
            return;
        }

        FileUpload fileUpload = fileUploads.iterator().next();
        String contentType = fileUpload.contentType();

        if (!contentType.startsWith("image/")) {
            fileSystem.deleteBlocking(fileUpload.uploadedFileName());
            route.response()
                    .setStatusCode(415)
                    .putHeader("Content-Type", "application/json")
                    .end(error("Expected image content types, but received: " + contentType).encodePrettily());
            return;
        }

        JsonObject imageFile = new JsonObject()
                .put("uploadedFileName", fileUpload.uploadedFileName())
                .put("originalFileName", fileUpload.fileName())
                .put("contentType", fileUpload.contentType())
                .put("size", fileUpload.size());

        vertx.eventBus().send("db.images.save", imageFile, (AsyncResult<Message<JsonObject>> async) -> {
            JsonObject result = async.result().body();

            if (result.containsKey("error")) {
                fileSystem.deleteBlocking(fileUpload.uploadedFileName());
                route.response().setStatusCode(500);
            }

            route.response()
                    .putHeader("Content-Type", "application/json")
                    .end(result.encodePrettily());
        });
    }

    private static String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replaceAll("[+]", "%20");
        } catch (UnsupportedEncodingException e) {
            // NB: MAY NOT be thrown for UTF-8 actually
            throw new UncheckedIOException(e);
        }
    }
}
