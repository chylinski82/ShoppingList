package com.example.shoppinglist;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// This class is an adapter for the RecyclerView to display a list of items.
// This is a custom RecyclerView.Adapter class specifically for the Item data model.
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    // Reference to the list of items that the adapter will display in the RecyclerView.
    // This is not a separate list, but a reference to the master list from MainActivity.
    // Therefore, any changes made here will also affect the itemList in MainActivity.
    private List<Item> itemsList;

    // Context is used for various Android framework operations, like inflating views.
    // Context is an Android concept; it's a handle to the system which allows
    // operations like accessing resources, launching activities, and so on.
    private final Context context;

    // Define a unified interface to handle various item actions.
    // Implement this interface in the activity or fragment to listen to item-related events.
    public interface ItemActionListener {
        // Callback for when an item is to be removed.
        // @param position: The position of the item to be removed.
        void onItemRemove(int position);

        // Callback for when an item's importance is to be set.
        // @param position The position of the item whose importance is to be set.
        // @param importance The importance level to set.
        void onItemImportanceChange(int position, Item.ImportanceLevel importance);

        // Callback for when a new item is to be added.
        void onItemAdd();
    }

    // ItemActionListener encompasses all individual listeners into one.
    // It allows us to notify the main activity (or any other listener) of different user actions on items.
    private ItemActionListener itemActionListener;

    // Set the listener for item-related actions.
    public void setItemActionListener(ItemActionListener listener) {
        this.itemActionListener = listener;
    }

    // Constructor for the adapter. It takes in a context and the list of items.
    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.itemsList = new ArrayList<>(items);
    }

    // This method centralizes the logic when an item's importance is set, either through user interaction
    // with the importance buttons or when 'enter' is pressed (which sets it to NORMAL)
    // @param holder The ViewHolder associated with the item.
    // @param importanceLevel The importance level to set for the item.
    private void handleItemAction(ItemViewHolder holder, Item.ImportanceLevel importanceLevel) {
        int position = holder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Item currentItem = itemsList.get(position);

            currentItem.setOptionsExpanded(false); // UI update
            if (currentItem.isNewEntry()) {
                currentItem.setNewEntry(false); // UI update
                if (itemActionListener != null) {
                    itemActionListener.onItemAdd();
                }
            }

            // Business logic handled by ViewModel via interface
            if (itemActionListener != null) {
                itemActionListener.onItemImportanceChange(position, importanceLevel);
            }
        }
    }

    // This method is called when a new ViewHolder is needed. This happens when the RecyclerView is laid out.
    // The created ViewHolder will be used to display items of the adapter using onBindViewHolder.
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We inflate the item layout. This turns the XML layout file into an actual View object.
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        // Return the ViewHolder instance.
        return new ItemViewHolder(view);
    }

    // This method binds the data to the ViewHolder.
    // This method is called by the RecyclerView to display data at a specific position in the list.
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Get the item from the list at the specified position.
        Item item = itemsList.get(position);

        // Method in ItemViewHolder to manage UI aspects like showing importance buttons and setting
        // background color of items based on importance
        holder.bindData(item, position);

    }

    // This method returns the size of the dataset.
    // It tells the RecyclerView how many items are in the list.
    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void updateData(List<Item> newData) {
        this.itemsList = newData;
        notifyDataSetChanged();
    }

    // This is the ViewHolder class.
    // It holds the views that will display the contents of a single item in our RecyclerView.
    // It's used to cache the views within the item layout for fast access.
    public class ItemViewHolder extends RecyclerView.ViewHolder {

        // These are references to the views in the item layout.
        EditText editTextItem;
        ImageButton optionsButton, removeButton, importantButton, normalButton,
                unimportantButton; // You can add references to other buttons here.

        // Constructor for the ViewHolder. The itemView is the root view of the item layout.
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views inside the item layout and assign them to the ViewHolder's variables.
            editTextItem = itemView.findViewById(R.id.editTextItem);
            optionsButton = itemView.findViewById(R.id.optionsButton);
            removeButton = itemView.findViewById(R.id.removeButton);
            importantButton = itemView.findViewById(R.id.importantButton);
            normalButton = itemView.findViewById(R.id.normalButton);
            unimportantButton = itemView.findViewById(R.id.unimportantButton); // Continue with finding other views...

            // This code sets up a listener so that we can respond to changes
            // in the EditText view. When user starts typing item name in the edit text view,
            // the importance buttons will appear to add the item to the list
            editTextItem.addTextChangedListener(new TextWatcher() {
                private boolean userTypedAtLeastOneChar = false;  // New flag to track if at least one char was typed

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                // This method is called when the text in the EditText changes.
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        userTypedAtLeastOneChar = true;  // Update the flag

                        // Detect Enter key without inserting a new line.
                        // Check if the last character is a newline.
                        if (s.charAt(s.length() - 1) == '\n') {
                            // Remove the newline
                            editTextItem.setText(s.subSequence(0, s.length() - 1));
                            editTextItem.setSelection(s.length() - 1); // Set the cursor to the end
                            int position = getBindingAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.NORMAL);
                            }

                            return; // Exit early, the other checks below are not necessary.
                        }

                        // Save the text to the Item object
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            itemsList.get(position).setText(s.toString());
                        }

                        // Show the importance buttons
                        importantButton.setVisibility(View.VISIBLE);
                        normalButton.setVisibility(View.VISIBLE);
                        unimportantButton.setVisibility(View.VISIBLE);
                        optionsButton.setVisibility(View.GONE);
                        removeButton.setVisibility(View.GONE);
                    } else {
                        // Hide the importance buttons
                        importantButton.setVisibility(View.GONE);
                        normalButton.setVisibility(View.GONE);
                        unimportantButton.setVisibility(View.GONE);
                        optionsButton.setVisibility(View.GONE);
                        removeButton.setVisibility(View.GONE);

                        // Remove the item if all characters are deleted, the user typed at least one char before,
                        // and it's not the last item in the list.
                        if (userTypedAtLeastOneChar) {
                            int position = getBindingAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && position < itemsList.size() - 1) {
                                // Call the onItemRemove/removeItem method from MainActivity
                                if (itemActionListener != null) {
                                    itemActionListener.onItemRemove(position);
                                }
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // This code sets up a listener on the optionsButton.
            // Clicking options button will disappear it, making visible
            // importance buttons instead.
            optionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Item item = itemsList.get(position);
                        // Toggle the options expansion status.
                        item.setOptionsExpanded(!item.isOptionsExpanded());
                        // Update the visibility directly
                        optionsButton.setVisibility(View.GONE);
                        importantButton.setVisibility(View.VISIBLE);
                        normalButton.setVisibility(View.VISIBLE);
                        unimportantButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            // For handling importance actions
            importantButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (itemActionListener != null && position != RecyclerView.NO_POSITION) {
                    Item currentItem = itemsList.get(position);

                    // UI updates
                    currentItem.setOptionsExpanded(false);
                    notifyItemChanged(position);  // This will invoke `bindData` again to reflect the changes

                    // Inform the ViewModel to update the data
                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.IMPORTANT);
                    if (currentItem.isNewEntry()) {
                        itemActionListener.onItemAdd();
                    }
                }
            });

            normalButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (itemActionListener != null && position != RecyclerView.NO_POSITION) {
                    Item currentItem = itemsList.get(position);

                    // UI updates
                    currentItem.setOptionsExpanded(false);
                    notifyItemChanged(position);  // This will invoke `bindData` again to reflect the changes

                    // Inform the ViewModel to update the data
                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.NORMAL);
                    if (currentItem.isNewEntry()) {
                        itemActionListener.onItemAdd();
                    }
                }
            });

            unimportantButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (itemActionListener != null && position != RecyclerView.NO_POSITION) {
                    Item currentItem = itemsList.get(position);

                    // UI updates
                    currentItem.setOptionsExpanded(false);
                    notifyItemChanged(position);  // This will invoke `bindData` again to reflect the changes

                    // Inform the ViewModel to update the data
                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.UNIMPORTANT);
                    if (currentItem.isNewEntry()) {
                        itemActionListener.onItemAdd();
                    }
                }
            });

            removeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemActionListener != null) {
                    itemActionListener.onItemRemove(position);
                }
            });

        }

        public void bindData(Item item, int position) {
            editTextItem.setText(item.getText());

            // 1. Handle focusing on the new entry.
            if(item.isNewEntry()) {
                editTextItem.requestFocus();

                // Open the keyboard.
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextItem, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            // 2. Handle visibility of optionsButton, removeButton, and importance buttons
            if (!item.getText().isEmpty()) {
                optionsButton.setVisibility(View.VISIBLE);
                removeButton.setVisibility(View.VISIBLE);
            } else {
                optionsButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.GONE);
            }

            if (item.isOptionsExpanded()) {
                importantButton.setVisibility(View.VISIBLE);
                normalButton.setVisibility(View.VISIBLE);
                unimportantButton.setVisibility(View.VISIBLE);
                optionsButton.setVisibility(View.GONE);
            } else {
                importantButton.setVisibility(View.GONE);
                normalButton.setVisibility(View.GONE);
                unimportantButton.setVisibility(View.GONE);
                optionsButton.setVisibility(item.getText().isEmpty() ? View.GONE : View.VISIBLE); // You may need this line to show the options button again when the importance is set and options are not expanded.
            }

            // 3. Handle importance color
            int colorRes;
            switch (item.getImportance()) {
                case IMPORTANT:
                    colorRes = (position % 2 == 0) ? R.color.color_important_even : R.color.color_important_odd;
                    break;
                case UNIMPORTANT:
                    colorRes = (position % 2 == 0) ? R.color.color_unimportant_even : R.color.color_unimportant_odd;
                    break;
                case NORMAL:
                default:
                    colorRes = (position % 2 == 0) ? R.color.color_normal_even : R.color.color_normal_odd;
                    break;
            }
            itemView.setBackgroundColor(ContextCompat.getColor(context, colorRes));

            // You can continue to add other UI update logic here...
        }

    }
}
