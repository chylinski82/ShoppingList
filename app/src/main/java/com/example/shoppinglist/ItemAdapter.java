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
import java.util.List;

// This class is an adapter for the RecyclerView to display a list of items.
// This is a custom RecyclerView.Adapter class specifically for the Item data model.
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    // Reference to the list of items that the adapter will display in the RecyclerView.
    // This is not a separate list, but a reference to the master list from MainActivity.
    // Therefore, any changes made here will also affect the itemList in MainActivity.
    private final List<Item> items;

    // Context is used for various Android framework operations, like inflating views.
    // Context is an Android concept; it's a handle to the system which allows
    // operations like accessing resources, launching activities, and so on.
    private final Context context;

    // Define an interface for item removal callback
    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    // Instance of the listener that will be set by the activity or fragment
    private OnItemRemoveListener removalListener;

    // Set the listener that will be called when an item is to be removed.
    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.removalListener = listener;
    }

    // Define an interface for items sort and refresh callback
    public interface OnSortAndRefreshListener {
        void onSortAndRefresh();
    }

    // Instance of the listener that will be set by the activity or fragment
    private OnSortAndRefreshListener sortAndRefreshListener;

    // Set the listener that will be called when list needs to be sorted and refreshed.
    public void setOnSortAndRefreshListener(OnSortAndRefreshListener listener) {
        this.sortAndRefreshListener = listener;
    }

    // Constructor for the adapter. It takes in a context and the list of items.
    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
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
        Item item = items.get(position);

        // Update the views inside the ViewHolder with the data from the item.
        holder.editTextItem.setText(item.getText());

        // Handle focusing on the new entry.
        if(item.isNewEntry()) {
            holder.editTextItem.requestFocus();

            // Open the keyboard.
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(holder.editTextItem, InputMethodManager.SHOW_IMPLICIT);
            }

        }

        // If the item has text, show the options and remove button.
        if (!item.getText().isEmpty()) {
            holder.optionsButton.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.VISIBLE);
        } else {
            holder.optionsButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);
        }

        // Determine the visibility of the importance buttons based on options expansion status.
        if (item.isOptionsExpanded()) {
            holder.importantButton.setVisibility(View.VISIBLE);
            holder.normalButton.setVisibility(View.VISIBLE);
            holder.unimportantButton.setVisibility(View.VISIBLE);
            holder.optionsButton.setVisibility(View.GONE);
        } else {
            holder.importantButton.setVisibility(View.GONE);
            holder.normalButton.setVisibility(View.GONE);
            holder.unimportantButton.setVisibility(View.GONE);
        }

        int colorRes; // This will store the color resource ID.

        // Determine the color based on importance and position.
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

        // Set the background color for the item.
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, colorRes));
    }

    // This method returns the size of the dataset.
    // It tells the RecyclerView how many items are in the list.
    @Override
    public int getItemCount() {
        return items.size();
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

                            handleNormalAction();

                            return; // Exit early, the other checks below are not necessary.
                        }

                        // Save the text to the Item object
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            items.get(position).setText(s.toString());
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
                            if (position != RecyclerView.NO_POSITION && position < items.size() - 1) {
                                // Call the onItemRemove/removeItem method from MainActivity
                                if (removalListener != null) {
                                    removalListener.onItemRemove(position);
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
                        Item item = items.get(position);
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

            // This code sets up a listener on the importantButton.
            // Clicking important button will disappear all importance buttons, making visible
            // options buttons instead.
            importantButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MainActivity mainActivity = (MainActivity) context;

                    Item currentItem = items.get(position);

                    // Set importance
                    mainActivity.setItemImportance(position, Item.ImportanceLevel.IMPORTANT);
                    items.get(position).setOptionsExpanded(false);

                    if (currentItem.isNewEntry()) {
                        // If it's a newly added item, add another new item
                        currentItem.setNewEntry(false);  // Update it to false, as it's no longer a new entry
                        mainActivity.addItem();
                    }

                    // Update UI buttons
                    importantButton.setVisibility(View.GONE);
                    normalButton.setVisibility(View.GONE);
                    unimportantButton.setVisibility(View.GONE);
                    optionsButton.setVisibility(View.VISIBLE);

                    // Sort items after adding a new one.
                    if (sortAndRefreshListener != null) {
                        sortAndRefreshListener.onSortAndRefresh();
                    }
                }
            });

            // Similar to important and unimportant buttons. The logic and comments are inside
            // handleNormalAction() further down below
            normalButton.setOnClickListener(v -> handleNormalAction());

            // This code sets up a listener on the unimportantButton.
            // Clicking unimportant button will disappear all importance buttons, making visible
            // options buttons instead.
            unimportantButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MainActivity mainActivity = (MainActivity) context;

                    Item currentItem = items.get(position);

                    //  Set importance
                    mainActivity.setItemImportance(position, Item.ImportanceLevel.UNIMPORTANT);
                    items.get(position).setOptionsExpanded(false);

                    if (currentItem.isNewEntry()) {
                        // If it's a newly added item, add another new item
                        currentItem.setNewEntry(false);  // Update it to false, as it's no longer a new entry
                        mainActivity.addItem();
                    }

                    // Update UI buttons
                    importantButton.setVisibility(View.GONE);
                    normalButton.setVisibility(View.GONE);
                    unimportantButton.setVisibility(View.GONE);
                    optionsButton.setVisibility(View.VISIBLE);

                    // Sort items after adding a new one.
                    if (sortAndRefreshListener != null) {
                        sortAndRefreshListener.onSortAndRefresh();
                    }
                }
            });

            //  Sets listener on remove button
            removeButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.removeItem(position);
                }
            });

        }

        // This code defines logic for tapping normalButton and 'enter'.
        // Clicking normal button or 'enter' will disappear all importance buttons, making visible
        // options buttons instead.
        private void handleNormalAction() {
            // Get the current position of the item in the RecyclerView
            int position = getBindingAdapterPosition();

            // Check if the position is valid
            if (position != RecyclerView.NO_POSITION) {
                // Get the instance of the MainActivity to access its methods
                MainActivity mainActivity = (MainActivity) context;

                // Fetch the current item being interacted with
                Item currentItem = items.get(position);

                // Set the importance of the current item to NORMAL
                mainActivity.setItemImportance(position, Item.ImportanceLevel.NORMAL);

                // Collapse any expanded options for this item
                items.get(position).setOptionsExpanded(false);

                // Check if this item is a newly added entry
                if (currentItem.isNewEntry()) {
                    // If it's a newly added item, we flag it as not new anymore
                    currentItem.setNewEntry(false);

                    // Add another new item for the next entry
                    mainActivity.addItem();
                }

                // Update the visibility of UI buttons for the current item
                // Hide importance buttons
                importantButton.setVisibility(View.GONE);
                normalButton.setVisibility(View.GONE);
                unimportantButton.setVisibility(View.GONE);

                // Show the options button
                optionsButton.setVisibility(View.VISIBLE);

                // After the item's importance is set, sort the list
                if (sortAndRefreshListener != null) {
                    sortAndRefreshListener.onSortAndRefresh();
                }
            }
        }
    }
}
