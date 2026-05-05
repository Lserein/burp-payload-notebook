package com.payloadnotebook.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCategory {
    private String id;
    private String name;
    private List<Entry> entries;

    public SubCategory() {
        this.id = UUID.randomUUID().toString();
        this.entries = new ArrayList<>();
    }

    public SubCategory(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.entries = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }

    @Override
    public String toString() { return name; }
}
