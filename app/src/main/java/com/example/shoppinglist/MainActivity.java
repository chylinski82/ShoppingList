package com.example.shoppinglist;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

// The main activity for the app. Implements the ItemAdapter.ItemActionListener to handle user interactions with list items.
public class MainActivity extends AppCompatActivity implements ItemAdapter.ItemActionListener {

    // Adapter to manage and display list items.
    private ItemAdapter itemAdapter;

    // ViewModel to manage the data and business logic for the app.
    private ItemsViewModel itemsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the undo button to revert the last action.
        ImageButton undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(v -> itemsViewModel.undo());

        // Initialize the RecyclerView to display the list items.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Instantiate the ViewModel.
        itemsViewModel = new ViewModelProvider(this).get(ItemsViewModel.class);

        // Observe changes to the items list. If there's a change, update the RecyclerView's data.
        itemsViewModel.getItemListLiveData().observe(this, updatedList -> {
            if (recyclerView.isComputingLayout()) {
                recyclerView.post(() -> itemAdapter.updateData(updatedList));
            } else {
                itemAdapter.updateData(updatedList);
            }
        });

        // Observe the flag for scrolling to the bottom of the list. If set, scroll to the last item.
        itemsViewModel.getScrollToBottom().observe(this, shouldScroll -> {
            if (shouldScroll) {
                int lastItemPosition = itemAdapter.getItemCount() - 1;
                recyclerView.getLayoutManager().scrollToPosition(lastItemPosition);
                itemsViewModel.resetScrollToBottomFlag();
            }
        });

        // Set the layout manager and adapter for the RecyclerView.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(this, itemsViewModel.getItemList());
        itemAdapter.setItemActionListener(this);
        recyclerView.setAdapter(itemAdapter);

        // Enable swipe gestures on items. Swiping left or right removes the item.
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    itemsViewModel.removeItem(position);
                }
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // Prevent swiping for new entries.
                int position = viewHolder.getBindingAdapterPosition();
                ItemAdapter adapter = (ItemAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    Item currentItem = adapter.getItemAt(position);
                    if (currentItem.isNewEntry()) {
                        return 0;
                    }
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // If the activity is being created for the first time (not a configuration change), add a new item.
        if (savedInstanceState == null) {
            itemsViewModel.addItem();
        }
    }

    // Implementation of the ItemActionListener interface methods. These methods serve as a bridge
    // between user interactions in the UI (via the adapter) and the underlying business logic/data processing
    // (via the ViewModel). By delegating these actions to the ViewModel, the activity adheres to the
    // MVVM (Model-View-ViewModel) design pattern, ensuring a separation of concerns and
    // enhancing maintainability and testability.

    @Override
    public void onItemRemove(int position) {
        itemsViewModel.removeItem(position);
    }

    @Override
    public void onItemImportanceChange(int position, Item.ImportanceLevel importance) {
        itemsViewModel.handleItemImportanceChange(position, importance);
    }

    @Override
    public void onSetItemOptionsExpanded(int position, boolean expanded) {
        itemsViewModel.setItemOptionsExpanded(position, expanded);
    }

    @Override
    public void onItemTextChanged(Item item, String originalText) {
        itemsViewModel.handleItemTextChanged(item, originalText);
    }
}
