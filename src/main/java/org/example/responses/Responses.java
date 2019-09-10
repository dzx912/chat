package org.example.responses;

import io.vertx.core.json.JsonObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Responses {
    public static JsonObject error(Throwable cause) {
        Writer message = new StringWriter();
        cause.printStackTrace(new PrintWriter(message, true));
        return error(message.toString());
    }

    public static JsonObject error(String message) {
        return new JsonObject().put("error", message);
    }

    public static JsonObject ok(Object result) {
        return new JsonObject().put("result", result);
    }
}
