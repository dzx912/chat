package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MongoDbVerticle extends AbstractVerticle {
    private MongoClient client;

    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", "my_DB"));
        vertx.eventBus().consumer("database.save", this::saveDb);
        vertx.eventBus().consumer("getHistory", this::getHistory);
        vertx.eventBus().consumer("saveImage", this::saveImage);
        vertx.eventBus().consumer("saveMultipleImages", this::saveMultipleImages);
        vertx.eventBus().consumer("getImage", this::getImage);
    }

    private void getHistory(Message<String> message) {
        client.find("message", new JsonObject(),
                result -> message.reply(Json.encode(result.result()))
        );
    }

    private void saveDb(Message<String> message) {
        client.insert("message", new JsonObject(message.body()), this::handler);
    }

    private void saveImage(Message<String> message) {
        try {
            client.save("image", (new JsonObject()
                            .put("binaryData", new JsonObject().put("$binary", Files.readAllBytes(Paths.get(message.body())))))
                    , result -> message.reply(result.result()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMultipleImages(Message<String> message) {
        List<String> files = Arrays.asList(message.body().split(","));
        files.forEach(file ->
        {
            try {
                client.save("image", (new JsonObject().put("binaryData"
                        , new JsonObject().put("$binary",Files.readAllBytes(Paths.get(file)))))
                        , result -> message.reply(result.result()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void getImage(Message<String> message) {
        client.findOne("image", new JsonObject().put("_id", message.body()), null
                , result -> message.reply(result.result() != null ?
                        result.result().getJsonObject("binaryData") : null));
        //,result -> System.out.println(query));
    }


    private void handler(AsyncResult<String> stringAsyncResult) {
        if (stringAsyncResult.succeeded()) {
            System.out.println("MongoDB save: " + stringAsyncResult.result());

        } else {
            System.out.println("ERROR MongoDB: " + stringAsyncResult.cause());
        }
    }
}
