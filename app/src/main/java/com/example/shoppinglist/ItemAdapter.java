package com.example.shoppinglist;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// This class is an adapter for the RecyclerView to display a list of items.
// This is a custom RecyclerView.Adapter class specifically for the Item data model.
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    // List of items that will be displayed in the RecyclerView.
    private List<Item> items;

    // Context is used for various Android framework operations, like inflating views.
    // Context is an Android concept; it's a handle to the system which allows
    // operations like accessing resources, launching activities, and so on.
    private Context context;

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

        // Change the background color based on importance.
        switch (item.getImportance()) {
            case IMPORTANT:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_important_odd));
                break;
            case UNIMPORTANT:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_unimportant_odd));
                break;
            case NORMAL:
            default:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_normal_odd));
                break;
        }
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
            // in the EditText view. When user starts typing item name in edit text view, the importance
            // buttons will appear to add the item to the list
            editTextItem.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                // This method is called when the text in the EditText changes.
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        // Based on the new text, we might change the visibility
                        // of other views in the ViewHolder.

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
                        removeButton.setVisibility(View.VISIBLE);
                    } else {
                        // Hide the importance buttons
                        importantButton.setVisibility(View.GONE);
                        normalButton.setVisibility(View.GONE);
                        unimportantButton.setVisibility(View.GONE);
                        optionsButton.setVisibility(View.GONE);
                        removeButton.setVisibility(View.GONE);
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

                    MainActivity.sortAndRefreshList();  // Sort items after adding a new one.
                }
            });

            // This code sets up a listener on the normalButton.
            // Clicking normal button will disappear all importance buttons, making visible
            // options buttons instead.
            normalButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MainActivity mainActivity = (MainActivity) context;

                    Item currentItem = items.get(position);

                    //  Set importance
                    mainActivity.setItemImportance(position, Item.ImportanceLevel.NORMAL);
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

                    MainActivity.sortAndRefreshList();  // Sort items after adding a new one.
                }
            });

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

                    MainActivity.sortAndRefreshList();  // Sort items after adding a new one.
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
    }
}
