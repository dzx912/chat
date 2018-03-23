package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoDbVerticle extends AbstractVerticle {
    private MongoClient client;
    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", "my_DB"));
        vertx.eventBus().consumer("database.save", this::saveDb);
        vertx.eventBus().consumer("getHistory", this::getHistory);
    }

    private void getHistory(Message<String> message) {
        client.find("message", new JsonObject(),
                result -> message.reply(Json.encode(result.result()))
        );
    }
    private void saveDb(Message<String> message) {
        client.insert("message", new JsonObject(message.body()), this::handler);
    }

    private void handler(AsyncResult<String> stringAsyncResult) {
        if (stringAsyncResult.succeeded()) {
            System.out.println("MongoDB save: " + stringAsyncResult.result());

        } else {
            System.out.println("ERROR MongoDB: " + stringAsyncResult.cause());
        }
    }
}
