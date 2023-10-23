// Package declaration.
package com.example.shoppinglist;

// Represents a single shopping list item in the application.
// This class serves as the model in the MVVM architecture, containing data and state related to each item.
public class Item {
    private long id; // Unique identifier for each item.
    private String text;  // Content/description of the shopping item.
    private boolean isOptionsExpanded; // State of item's options visibility.
    private boolean isNewEntry; // Flag indicating if the item is newly created.
    private ImportanceLevel importance;  // Enum indicating the priority of the item.

    // Constructor to initialize an item with its essential properties.
    public Item(long id, String text, boolean isNewEntry) {
        this.id = id;
        this.text = text;
        this.isOptionsExpanded = false;
        this.isNewEntry = isNewEntry;
        this.importance = ImportanceLevel.NORMAL;
    }

    // Overloaded constructor for creating an item with default 'newEntry' status.
    public Item(long id, String text) {
        this(id, text, true);  // Default to new entry.
    }

    // Standard getters and setters.
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public boolean isOptionsExpanded() {
        return isOptionsExpanded;
    }
    public void setOptionsExpanded(boolean optionsExpanded) {
        isOptionsExpanded = optionsExpanded;
    }
    public boolean isNewEntry() {
        return isNewEntry;
    }
    public void setNewEntry(boolean newEntry) {
        isNewEntry = newEntry;
    }
    public ImportanceLevel getImportance() {
        return importance;
    }
    public void setImportance(ImportanceLevel importance) {
        this.importance = importance;
    }

    // Enum representing importance levels for a shopping item.
    public enum ImportanceLevel {
        IMPORTANT, NORMAL, UNIMPORTANT
    }
}
