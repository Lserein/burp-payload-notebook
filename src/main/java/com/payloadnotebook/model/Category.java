package com.payloadnotebook.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Category {
    private String id;
    private String name;
    private boolean expanded;
    private List<SubCategory> subCategories;

    public Category() {
        this.id = UUID.randomUUID().toString();
        this.expanded = true;
        this.subCategories = new ArrayList<>();
    }

    public Category(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.expanded = true;
        this.subCategories = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    public List<SubCategory> getSubCategories() { return subCategories; }
    public void setSubCategories(List<SubCategory> subCategories) { this.subCategories = subCategories; }

    @Override
    public String toString() { return name; }
}
