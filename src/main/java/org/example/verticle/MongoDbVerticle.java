package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class MongoDbVerticle extends AbstractVerticle {
    private MongoClient client;
    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject().put("db_name", "my_DB"));
        vertx.eventBus().consumer("db.message.save", this::messageSave);
        vertx.eventBus().consumer("db.message.history", this::messageHistory);
    }

    private void messageHistory(Message<String> message) {
        client.find("message", new JsonObject(), messageHistoryHandler(message));
    }

    private Handler<AsyncResult<List<JsonObject>>> messageHistoryHandler(Message<String> message) {
        return result -> message.reply(Json.encode(result.result()));
    }

    private void messageSave(Message<String> message) {
        client.insert("message", new JsonObject(message.body()), this::messageSaveHandler);
    }

    private void messageSaveHandler(AsyncResult<String> result) {
        if (result.succeeded()) {
            System.out.println("MongoDB save: " + result.result());

        } else {
            System.out.println("ERROR MongoDB: " + result.cause());
        }
    }
}
