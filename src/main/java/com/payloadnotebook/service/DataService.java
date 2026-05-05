package com.payloadnotebook.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payloadnotebook.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataService {

    private static final String DATA_FILE = "payload_notebook.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private NotebookData data;
    private File dataFile;

    private final File defaultDataFile;

    public DataService(File extensionDir) {
        this.defaultDataFile = new File(extensionDir, DATA_FILE);
        this.dataFile = defaultDataFile;
        loadData();
    }

    public NotebookData getData() {
        return data;
    }

    public File getDataFile() {
        return dataFile;
    }

    public File getDefaultDataFile() {
        return defaultDataFile;
    }

    /**
     * Switch the active data file to a custom path and reload.
     */
    public boolean loadFromFile(File file) {
        if (file == null || !file.exists()) return false;
        this.dataFile = file;
        loadData();
        return true;
    }

    /**
     * Switch back to the default data file and reload.
     */
    public void loadDefault() {
        this.dataFile = defaultDataFile;
        loadData();
    }

    // --- Lifecycle ---

    public void loadData() {
        if (dataFile.exists()) {
            try (Reader reader = Files.newBufferedReader(dataFile.toPath(), StandardCharsets.UTF_8)) {
                data = GSON.fromJson(reader, NotebookData.class);
                if (data != null && data.getCategories() != null) {
                    return;
                }
            } catch (Exception e) {
                System.err.println("[Payload Notebook] Failed to load data: " + e.getMessage());
            }
        }
        loadDefaultData();
    }

    private void loadDefaultData() {
        try (InputStream is = getClass().getResourceAsStream("/default_data.json")) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    data = GSON.fromJson(reader, NotebookData.class);
                    if (data != null) return;
                }
            }
        } catch (Exception e) {
            System.err.println("[Payload Notebook] Failed to load default data: " + e.getMessage());
        }
        data = new NotebookData();
    }

    public void saveData() {
        try {
            dataFile.getParentFile().mkdirs();
            try (Writer writer = Files.newBufferedWriter(dataFile.toPath(), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[Payload Notebook] Failed to save data: " + e.getMessage());
        }
    }

    // --- Category operations ---

    public Category addCategory(String name) {
        Category cat = new Category(name);
        data.getCategories().add(cat);
        saveData();
        return cat;
    }

    public void renameCategory(String id, String newName) {
        Category cat = findCategory(id);
        if (cat != null) {
            cat.setName(newName);
            saveData();
        }
    }

    public void deleteCategory(String id) {
        data.getCategories().removeIf(c -> c.getId().equals(id));
        saveData();
    }

    public Category findCategory(String id) {
        return data.getCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
    }

    // --- SubCategory operations ---

    public SubCategory addSubCategory(String parentCategoryId, String name) {
        Category parent = findCategory(parentCategoryId);
        if (parent != null) {
            SubCategory sub = new SubCategory(name);
            parent.getSubCategories().add(sub);
            saveData();
            return sub;
        }
        return null;
    }

    public void renameSubCategory(String id, String newName) {
        SubCategory sub = findSubCategory(id);
        if (sub != null) {
            sub.setName(newName);
            saveData();
        }
    }

    public void deleteSubCategory(String id) {
        for (Category cat : data.getCategories()) {
            if (cat.getSubCategories().removeIf(s -> s.getId().equals(id))) {
                saveData();
                return;
            }
        }
    }

    public SubCategory findSubCategory(String id) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                if (sub.getId().equals(id)) {
                    return sub;
                }
            }
        }
        return null;
    }

    public Category findParentCategory(String subCategoryId) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                if (sub.getId().equals(subCategoryId)) {
                    return cat;
                }
            }
        }
        return null;
    }

    // --- Entry operations ---

    public Entry addEntry(String subCategoryId, String title, String payload) {
        SubCategory sub = findSubCategory(subCategoryId);
        if (sub != null) {
            Entry entry = new Entry(title, payload);
            sub.getEntries().add(entry);
            saveData();
            return entry;
        }
        return null;
    }

    public void updateEntry(String entryId, String title, String payload, String newSubCategoryId) {
        Entry entry = findEntry(entryId);
        if (entry == null) return;

        // Find current subcategory
        SubCategory currentSub = findSubCategoryByEntry(entryId);
        if (currentSub == null) return;

        // If subcategory changed, move entry
        if (newSubCategoryId != null && !newSubCategoryId.equals(currentSub.getId())) {
            currentSub.getEntries().remove(entry);
            SubCategory newSub = findSubCategory(newSubCategoryId);
            if (newSub != null) {
                newSub.getEntries().add(entry);
            }
        }

        entry.setTitle(title);
        entry.setPayload(payload);
        saveData();
    }

    public void deleteEntry(String entryId) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                if (sub.getEntries().removeIf(e -> e.getId().equals(entryId))) {
                    saveData();
                    return;
                }
            }
        }
    }

    public Entry findEntry(String entryId) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                for (Entry entry : sub.getEntries()) {
                    if (entry.getId().equals(entryId)) {
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    public SubCategory findSubCategoryByEntry(String entryId) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                for (Entry entry : sub.getEntries()) {
                    if (entry.getId().equals(entryId)) {
                        return sub;
                    }
                }
            }
        }
        return null;
    }

    // --- Query ---

    public List<Entry> getAllEntries() {
        List<Entry> all = new ArrayList<>();
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                all.addAll(sub.getEntries());
            }
        }
        return all;
    }

    public List<Entry> getEntriesByCategory(String categoryId) {
        Category cat = findCategory(categoryId);
        if (cat == null) return new ArrayList<>();
        List<Entry> entries = new ArrayList<>();
        for (SubCategory sub : cat.getSubCategories()) {
            entries.addAll(sub.getEntries());
        }
        return entries;
    }

    public List<Entry> getEntriesBySubCategory(String subCategoryId) {
        SubCategory sub = findSubCategory(subCategoryId);
        return sub != null ? new ArrayList<>(sub.getEntries()) : new ArrayList<>();
    }

    public List<Entry> search(String keyword, String categoryId, String subCategoryId) {
        List<Entry> source;
        if (subCategoryId != null) {
            source = getEntriesBySubCategory(subCategoryId);
        } else if (categoryId != null) {
            source = getEntriesByCategory(categoryId);
        } else {
            source = getAllEntries();
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return source;
        }

        String lower = keyword.toLowerCase().trim();
        return source.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lower)
                        || (e.getPayload() != null && e.getPayload().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    public String getCategoryPath(String entryId) {
        for (Category cat : data.getCategories()) {
            for (SubCategory sub : cat.getSubCategories()) {
                for (Entry entry : sub.getEntries()) {
                    if (entry.getId().equals(entryId)) {
                        return cat.getName() + " > " + sub.getName();
                    }
                }
            }
        }
        return "";
    }

    public List<SubCategory> getAllSubCategories() {
        List<SubCategory> all = new ArrayList<>();
        for (Category cat : data.getCategories()) {
            all.addAll(cat.getSubCategories());
        }
        return all;
    }
}
