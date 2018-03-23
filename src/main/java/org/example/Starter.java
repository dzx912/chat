package org.example;

import io.vertx.core.Vertx;
import org.example.verticle.*;

public class Starter {
    public static void main(String[] args) {
        deploy(Vertx.vertx());
    }

    private static void deploy(Vertx vertx) {
        vertx.deployVerticle(new WsServerVerticle());
        vertx.deployVerticle(new RestServerVerticle());
        vertx.deployVerticle(new ClientServerVerticle());
        vertx.deployVerticle(new RouterVerticle());
        vertx.deployVerticle(new MongoDbVerticle());
    }
}
