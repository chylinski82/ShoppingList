package com.example.shoppinglist;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ItemsViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // LiveData holding the list of items. MutableLiveData lets us modify the data.
    private final MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>(new ArrayList<>());

    // Provides access to the itemListLiveData to observe from UI components.
    public MutableLiveData<List<Item>> getItemListLiveData() {
        return itemListLiveData;
    }

    // Gets the current list of items. Useful for immediate access.
    public List<Item> getItemList() {
        return itemListLiveData.getValue();
    }

    // LiveData flag to indicate if the UI should scroll to the list's bottom.
    private final MutableLiveData<Boolean> scrollToBottom = new MutableLiveData<>(false);

    // Returns the LiveData flag to determine if UI should scroll to bottom.
    public LiveData<Boolean> getScrollToBottom() {
        return scrollToBottom;
    }

    // Triggers the UI to scroll to the bottom of the list.
    public void triggerScrollToBottom() {
        scrollToBottom.setValue(true);
    }

    // Resets the scrolling flag, so the UI won't auto-scroll again until re-triggered.
    public void resetScrollToBottomFlag() {
        scrollToBottom.setValue(false);
    }

    // Stack to keep track of the recent actions performed by the user for undo functionality.
    private Stack<Action> actionHistory = new Stack<>();

    // Inner static class representing an action performed by the user.
    public static class Action {

        // Enum to identify the type of action performed.
        public enum ActionType {
            REMOVE,           // Item was removed
            UPDATE_TEXT,      // Item's text was updated
            UPDATE_IMPORTANCE // Item's importance level was updated
        }

        // The type of action.
        private ActionType type;

        // The item on which the action was performed.
        private Item item;

        // Previous text of the item, if the action was a text update.
        private String previousText;

        // Previous importance level, if the action was an importance update.
        private Item.ImportanceLevel previousImportance;

        // Constructor to initialize an Action object.
        public Action(ActionType type, Item item, String previousText, Item.ImportanceLevel previousImportance) {
            this.type = type;
            this.item = item;
            this.previousText = previousText;
            this.previousImportance = previousImportance;
        }

        // Getter for action type.
        public ActionType getType() {
            return type;
        }

        // Setter for action type.
        public void setType(ActionType type) {
            this.type = type;
        }

        // Getter for the item.
        public Item getItem() {
            return item;
        }

        // Setter for the item.
        public void setItem(Item item) {
            this.item = item;
        }

        // Getter for the previous text.
        public String getPreviousText() {
            return previousText;
        }

        // Setter for the previous text.
        public void setPreviousText(String previousText) {
            this.previousText = previousText;
        }

        // Getter for the previous importance level.
        public Item.ImportanceLevel getPreviousImportance() {
            return previousImportance;
        }

        // Setter for the previous importance level.
        public void setPreviousImportance(Item.ImportanceLevel previousImportance) {
            this.previousImportance = previousImportance;
        }
    }

    public void fetchShoppingListItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document("UserID-0000").collection("ShoppingList")
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Log.e("ItemsViewModel", "Firestore fetch error: " + e.getMessage());
                        return;
                    }

                    List<Item> itemList = new ArrayList<>(getItemList()); // Get current list
                    Log.d("ItemsViewModel", "Fetched Items: " + itemList);

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot document = dc.getDocument();
                        Log.d("ItemsViewModel", "Document Change: " + dc.getType() + ", Document: " + document.getData());
                        switch (dc.getType()) {
                            case ADDED:
                                itemList.add(documentToItem(document));
                                break;
                            case MODIFIED:
                                updateItemInList(itemList, documentToItem(document));
                                break;
                            case REMOVED:
                                removeItemFromList(itemList, document.getId());
                                break;
                        }
                    }

                    itemListLiveData.setValue(itemList); // Update LiveData
                    sortAndRefreshList();
                });
    }

    private Item documentToItem(QueryDocumentSnapshot document) {
        long id = document.getLong("id");
        String text = document.getString("text");
        String importanceStr = document.getString("importance");
        Item.ImportanceLevel importance = Item.ImportanceLevel.valueOf(importanceStr.toUpperCase());

        Item item = new Item(id, text);
        item.setImportance(importance);

        // Set default values for isNewEntry and isOptionsExpanded
        item.setNewEntry(false); // Since it's fetched from Firestore, it's not a new entry
        item.setOptionsExpanded(false); // Default state for options expanded

        return item;
    }

    private void updateItemInList(List<Item> itemList, Item newItem) {
        Log.d("ItemsViewModel", "updateItemInList - Before Update: " + itemList);
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getId() == newItem.getId()) {
                itemList.set(i, newItem);
                return;
            }
        }
        Log.d("ItemsViewModel", "updateItemInList - After Update: " + itemList);
    }

    private void removeItemFromList(List<Item> itemList, String documentId) {
        itemList.removeIf(item -> String.valueOf(item.getId()).equals(documentId));
    }

    // Method to add a new item to the list.
    public void addItem() {
        // Create a new item with current timestamp as its ID and empty text.
        Item item = new Item(System.currentTimeMillis(), "");
        // Add the new item to Firestore
        addItemToFirestore(item);
        // Fetch the current item list.
        List<Item> currentList = getItemList();
        // Add the new item to the list.
        currentList.add(item);
        // Update the live data to notify observers of this change.
        itemListLiveData.setValue(currentList);
        // Scroll to the bottom of the list.
        triggerScrollToBottom();
    }

    private void addItemToFirestore(Item item) {
        Map<String, Object> itemMap = convertItemToMap(item);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document("UserID-0000").collection("ShoppingList")
                .document(String.valueOf(item.getId()))
                .set(itemMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ItemsViewModel", "New item added to Firestore with ID: " + item.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemsViewModel", "Error adding new item to Firestore", e);
                });
    }

    // Method to remove an item at a given position.
    public void removeItem(int position) {
        List<Item> currentList = getItemList();
        if (position >= 0 && position < currentList.size()) {
            // Get the item to be removed.
            Item removedItem = currentList.get(position);
            // Add a REMOVE action to the action history.
            actionHistory.push(new Action(Action.ActionType.REMOVE, removedItem, null, null));
            // Remove the item from the list.
            currentList.remove(position);
            // Update the live data to notify observers of this change.
            itemListLiveData.setValue(currentList);
        }
    }

    // Method to handle the change in importance level of an item.
    public void handleItemImportanceChange(int position, Item.ImportanceLevel importance) {
        List<Item> currentItems = getItemList();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            Item currentItem = currentItems.get(position);
            // Add an UPDATE_IMPORTANCE action to the action history.
            actionHistory.push(new Action(Action.ActionType.UPDATE_IMPORTANCE, currentItem, null, currentItem.getImportance()));
            if (currentItem.isNewEntry()) {
                currentItem.setNewEntry(false);
                addItem();
            }
            currentItem.setImportance(importance);
            // Sort and refresh the list based on new changes.
            sortAndRefreshList();
        }
    }

    // Method to sort the list based on the importance level and refresh it.
    public void sortAndRefreshList() {
        List<Item> currentList = getItemList();
        // Sort the list based on importance and empty text entries.
        currentList.sort((item1, item2) -> {
            if (item1.getText().isEmpty()) return 1;
            if (item2.getText().isEmpty()) return -1;
            return item1.getImportance().ordinal() - item2.getImportance().ordinal();
        });
        // Update the live data to notify observers of this change.
        itemListLiveData.setValue(currentList);
    }

    // Method to record text changes of an item for undo purposes.
    public void handleItemTextChanged(Item item, String originalText) {
        Log.d("ItemsViewModel", "handleItemTextChanged: Original Text: " + originalText + ", New Text: " + item.getText());
        // Add an UPDATE_TEXT action to the action history.
        // Note: The actual handling of text changes is done within the adapter.
        // This method only records the change for potential undo operations.
        actionHistory.push(new Action(Action.ActionType.UPDATE_TEXT, item, originalText, null));
        // Update item text in Firestore
        updateItemTextInFirestore(item);

        sortAndRefreshList();
    }

    private void updateItemTextInFirestore(Item item) {
        if (item.getId() == 0) return; // Skip if the item has no valid ID (not synced with Firestore)

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document("UserID-0000").collection("ShoppingList")
                .whereEqualTo("id", item.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming 'id' is unique and only one document is returned
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        documentSnapshot.getReference()
                                .update("text", item.getText())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ItemsViewModel", "Item text updated in Firestore for ID: " + item.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ItemsViewModel", "Error updating item text in Firestore", e);
                                });
                    } else {
                        Log.d("ItemsViewModel", "No matching document found in Firestore for ID: " + item.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemsViewModel", "Error finding item in Firestore for update", e);
                });
    }

    // Method to expand or collapse options for an item.
    public void setItemOptionsExpanded(int position, boolean expanded) {
        List<Item> currentList = getItemList();
        Item currentItem = currentList.get(position);
        currentItem.setOptionsExpanded(expanded);
        // Update the live data to notify observers of this change.
        itemListLiveData.setValue(currentList);
    }

    // Method to undo the last action performed.
    public void undo() {
        // Return if there's no action to undo.
        if (actionHistory.isEmpty()) return;
        Action lastAction = actionHistory.pop();
        List<Item> currentList = getItemList();
        switch (lastAction.getType()) {
            case REMOVE:
                // For REMOVE, add the item back to the list.
                currentList.add(lastAction.getItem());
                sortAndRefreshList();
                break;
            case UPDATE_TEXT:
                // For UPDATE_TEXT, revert the text to its previous value.
                Item itemForTextUpdate = findItemById(lastAction.getItem().getId());
                itemForTextUpdate.setText(lastAction.getPreviousText());
                break;
            case UPDATE_IMPORTANCE:
                // For UPDATE_IMPORTANCE, revert the importance level to its previous value.
                Item itemForImportanceUpdate = findItemById(lastAction.getItem().getId());
                itemForImportanceUpdate.setImportance(lastAction.getPreviousImportance());
                sortAndRefreshList();
                break;
        }
        // Update the live data to notify observers of this change.
        itemListLiveData.setValue(currentList);
    }

    // Helper method to find an item by its ID.
    private Item findItemById(long id) {
        for (Item item : getItemList()) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    // This utility method converts an Item object to a Map<String, Object> for Firestore operations.
    private Map<String, Object> convertItemToMap(Item item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", item.getId());
        itemMap.put("text", item.getText());
        itemMap.put("importance", item.getImportance().toString());
        itemMap.put("isNewEntry", item.isNewEntry());
        itemMap.put("isOptionsExpanded", item.isOptionsExpanded());
        // Add other fields as necessary
        return itemMap;
    }

    // method to check if the current list already has an empty item, employed in onCreate()
    public boolean hasEmptyItem() {
        List<Item> currentList = getItemList();
        return currentList.stream().anyMatch(item -> item.getText().isEmpty());
    }

}
