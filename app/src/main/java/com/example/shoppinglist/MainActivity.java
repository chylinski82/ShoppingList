package com.example.shoppinglist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Reference to the RecyclerView UI component
    private RecyclerView recyclerView;

    // Adapter bridges the data and the RecyclerView,
    // determining how each individual item should be displayed.
    private static ItemAdapter itemAdapter;

    // A list to hold our shopping items
    private static List<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the RecyclerView in the layout
        recyclerView = findViewById(R.id.recyclerView);

        // Setting up RecyclerView with a linear layout manager
        // The LayoutManager dictates the manner in which items are arranged on the screen
        // Here, we use LinearLayoutManager which arranges items in a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the RecyclerView with an adapter
        // Initialize with the current list of items
        // Initialize with empty list for now
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        // TODO: Initialize your data or load it from a source if needed
        // For now, adding a blank item as a starting point:
        addItem();
    }

    // Method to add a new item to the list
    public void addItem() {
        // Create a new empty item and add it to the list
        // For simplicity, using current time in milliseconds as ID (just an example)
        Item item = new Item(System.currentTimeMillis(), "");
        itemList.add(item);
        // Notify the adapter that a new item has been inserted to the list
        itemAdapter.notifyItemInserted(itemList.size() - 1);

    }

    // Method to remove an item from a specific position
    public void removeItem(int position) {
        if(position >= 0 && position < itemList.size()) {
            itemList.remove(position);
            // Notify the adapter that an item has been removed
            itemAdapter.notifyItemRemoved(position);
        }
    }

    // Method to set the importance level for an item
    public void setItemImportance(int position, Item.ImportanceLevel importance) {
        Item currentItem = itemList.get(position);
        currentItem.setImportance(importance);
        // Notify the adapter that the data for an item has changed and needs to be re-displayed
        itemAdapter.notifyItemChanged(position);

    }

    // Static method to sort items by their importance level
    public static void sortAndRefreshList() {
        itemList.sort((item1, item2) -> {
            // Place empty item at the bottom.
            if (item1.getText().isEmpty()) return 1;
            if (item2.getText().isEmpty()) return -1;

            // Compare importance levels.
            return item1.getImportance().ordinal() - item2.getImportance().ordinal();
        });
        // Notify the adapter that the dataset has changed
        // This will force all items to be redrawn
        itemAdapter.notifyDataSetChanged();
    }

}
