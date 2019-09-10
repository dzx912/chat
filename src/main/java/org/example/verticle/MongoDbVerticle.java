package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

import static org.example.responses.Responses.error;
import static org.example.responses.Responses.ok;

public class MongoDbVerticle extends AbstractVerticle {
    private MongoClient client;
    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject().put("db_name", "my_DB"));
        vertx.eventBus().consumer("db.message.save", this::messageSave);
        vertx.eventBus().consumer("db.message.history", this::messageHistory);
        vertx.eventBus().consumer("db.images.get", this::imagesGet);
        vertx.eventBus().consumer("db.images.getById", this::imagesGetById);
        vertx.eventBus().consumer("db.images.save", this::imagesSave);
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

    private void imagesGet(Message<Void> message) {
        client.find("images", new JsonObject(), async -> {
            JsonObject result;

            if (async.failed()) {
                result = error(async.cause());

            } else {
                JsonArray items = new JsonArray();
                async.result().forEach(items::add);
                result = ok(items);
            }

            message.reply(result);
        });
    }

    private void imagesGetById(Message<String> message) {
        JsonObject query = new JsonObject().put("_id", message.body());

        client.find("images", query, async -> {
            JsonObject result;

            if (async.failed()) {
                result = error(async.cause());

            } else {
                // NB: search by _id returns no more than 1 document
                JsonObject item = async.result().stream().findAny().orElse(null);
                result = ok(item);
            }

            message.reply(result);
        });
    }

    private void imagesSave(Message<JsonObject> message) {
        JsonObject image = message.body();

        client.insert("images", image, async -> message.reply(async.failed() ? error(async.cause()) : ok(image)));
    }
}
