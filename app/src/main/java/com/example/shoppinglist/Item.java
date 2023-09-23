package com.example.shoppinglist;

public class Item {
    private long id; // A unique identifier for each item, useful for differentiating items in the list.
    private String text;  // Represents the content or description of the shopping item (EditText view).

    // Indicates if the item's options (e.g., edit, delete) are currently displayed.
    // This might be used by the adapter to decide how to display the item in the RecyclerView.
    private boolean isOptionsExpanded;

    // Flag to identify if this is a newly created item. This could help in providing
    // certain UI indications or behaviors for new entries.
    private boolean isNewEntry;

    // Indicates the priority or significance of the item.
    // The adapter might use this to vary the display of items based on their importance.
    private ImportanceLevel importance;  // Importance level.

    // Primary constructor for creating a new item with specified parameters.
    public Item(long id, String text, boolean isNewEntry) {
        this.id = id;
        this.text = text;
        this.isOptionsExpanded = false;
        this.isNewEntry = isNewEntry;
        this.importance = ImportanceLevel.NORMAL;
    }

    // Overloaded constructor: For convenience,
    // when creating an item without specifying its 'newEntry' status.
    public Item(long id, String text) {
        this(id, text, true);  // By default, a new item will be marked as a new entry
    }

    // Standard getters and setters for the Item properties.
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

    // Enum to represent the various levels of importance an item can have.
    public enum ImportanceLevel {
        IMPORTANT, NORMAL, UNIMPORTANT
    }
}
