package com.example.shoppinglist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

// This ViewModel will manage and hold our data related to items.
public class ItemsViewModel extends ViewModel {

    // Master list to hold our shopping items. This is the primary data source for the entire application.
    // Any changes to this list will reflect across components that have a reference to this list.
    // ...then... Changed itemList to MutableLiveData to enable observing its changes
    private final MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>(new ArrayList<>());

    // Getter method for the MutableLiveData
    public MutableLiveData<List<Item>> getItemListLiveData() {
        return itemListLiveData;
    }

    // Method to retrieve the current list of items.
    // It's better to return a copy or an unmodifiable list to ensure no external mutations.
    // But for simplicity, we return the list directly here.
    public List<Item> getItemList() {
        return itemListLiveData.getValue();
    }

    // When modifying the list, ensure that you first retrieve a reference,
    // modify it, then post the updated list back to MutableLiveData.

    // Method to add a new item to the list
    public void addItem() {
        // Create a new empty item and add it to the list
        // For simplicity, using current time in milliseconds as ID (just an example)
        Item item = new Item(System.currentTimeMillis(), "");
        List<Item> currentList = getItemList();
        currentList.add(item);
        itemListLiveData.setValue(currentList); // Updating the MutableLiveData

    }

    // Method to remove an item from a specific position
    public void removeItem(int position) {
        List<Item> currentList = getItemList();
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            itemListLiveData.setValue(currentList); // Updating the MutableLiveData
        }
    }

    public void handleItemImportanceChange(int position, Item.ImportanceLevel importance) {
        List<Item> currentItems = itemListLiveData.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            Item currentItem = currentItems.get(position);

            // Handle new entry:
            if (currentItem.isNewEntry()) {
                currentItem.setNewEntry(false);
                addItem();  // Assuming the addItem() method adds to itemListLiveData.
            }

            // Set importance:
            currentItem.setImportance(importance);

            sortAndRefreshList();
        }
    }

    // Static method to sort items by their importance level
    public void sortAndRefreshList() {
        List<Item> currentList = getItemList();
        currentList.sort((item1, item2) -> {
            // Place empty item at the bottom.
            if (item1.getText().isEmpty()) return 1;
            if (item2.getText().isEmpty()) return -1;

            // Compare importance levels.
            return item1.getImportance().ordinal() - item2.getImportance().ordinal();
        });
        itemListLiveData.setValue(currentList); // Updating the MutableLiveData
    }

}
