package com.payloadnotebook.model;

import java.util.UUID;

public class Entry {
    private String id;
    private String title;
    private String payload;

    public Entry() {
        this.id = UUID.randomUUID().toString();
    }

    public Entry(String title, String payload) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @Override
    public String toString() { return title; }
}
