package org.example.data;

import io.vertx.core.json.Json;

public class Data {
    private String address;
    private String text;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Json.encodePrettily(this);
    }
}
