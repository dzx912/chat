package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import org.example.data.Data;

public class RouterVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("router", this::router);
    }

    private void router(Message<String> message) {
        if (message.body() != null && !message.body().isEmpty()) {
            System.out.println("Router message: " + message.body());
            Data data = Json.decodeValue(message.body(), Data.class);
            System.out.println(data);
            vertx.eventBus().send("/token/" + data.getAddress(), message.body());

            // Сохраняем сообщение в БД
            vertx.eventBus().send("database.save", message.body());
        }
    }
}
