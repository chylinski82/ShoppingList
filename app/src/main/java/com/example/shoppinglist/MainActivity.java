package com.example.shoppinglist;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements ItemAdapter.ItemActionListener {

    // Adapter bridges the data and the RecyclerView,
    // determining how each individual item should be displayed.
    private ItemAdapter itemAdapter;

    // Declare the ViewModel to hold our items list
    private ItemsViewModel itemsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ViewModel
        itemsViewModel = new ViewModelProvider(this).get(ItemsViewModel.class);
        //  Observe the MutableLiveData
        itemsViewModel.getItemListLiveData().observe(this, updatedList -> {
            // When the observed data changes, update the adapter's data and notify it
            itemAdapter.updateData(updatedList);
        });

        // Find the RecyclerView in the layout
        // Reference to the RecyclerView UI component
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Setting up RecyclerView with a linear layout manager
        // The LayoutManager dictates the manner in which items are arranged on the screen
        // Here, we use LinearLayoutManager which arranges items in a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the RecyclerView with an adapter
        // Instead of using the local itemList, now we retrieve it from the ViewModel
        itemAdapter = new ItemAdapter(this, itemsViewModel.getItemList());
        // Set the listeners for the adapter
        itemAdapter.setItemActionListener(this); // 'this' because MainActivity implements the listener.
        recyclerView.setAdapter(itemAdapter);

        // TODO: Initialize your data or load it from a source if needed
        // For now, adding a blank item as a starting point:
            // Check if activity is freshly created or recreated after configuration change.
        if (savedInstanceState == null) {
            itemsViewModel.addItem();  // Using ViewModel's addItem method
        }
    }

    @Override
    public void onItemRemove(int position) {
        // Callback method triggered when an item is set to be removed via the adapter's interface.
        // Delegates the actual removal logic to the removeItem method.
        itemsViewModel.removeItem(position); // Using ViewModel's removeItem method
        itemAdapter.notifyItemRemoved(position);
        itemAdapter.notifyItemRangeChanged(position, itemsViewModel.getItemList().size() - position);
    }

    @Override
    // Callback method triggered when the list is set to be sorted via the adapter's interface.
    // Delegates the actual sorting logic to the sortAndRefreshList method.
    public void onSortAndRefresh() {
        itemsViewModel.sortAndRefreshList(); // Using ViewModel's sortAndRefreshList method
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemImportanceSet(int position, Item.ImportanceLevel importance) {
        itemsViewModel.setItemImportance(position, importance);
        itemAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemAdd() {
        itemsViewModel.addItem();
        itemAdapter.notifyItemInserted(itemsViewModel.getItemList().size() - 1);
    }
}
