package com.payloadnotebook.model;

import java.util.ArrayList;
import java.util.List;

public class NotebookData {
    private List<Category> categories;

    public NotebookData() {
        this.categories = new ArrayList<>();
    }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
}
