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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Adapter for handling a list of items within a RecyclerView.
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    // List that contains the items.
    private List<Item> itemsList;

    // Context is often used for getting resources or invoking system methods.
    private final Context context;

    // Interface to communicate actions taken on items to the outside.
    public interface ItemActionListener {
        void onItemRemove(int position);  // Triggered when an item is removed.
        void onItemImportanceChange(int position, Item.ImportanceLevel importance);  // Triggered when an item's importance changes.
        void onSetItemOptionsExpanded(int position, boolean expanded);  // Triggered when an item's options are expanded/collapsed.
        void onItemTextChanged(Item item, String originalText);  // Triggered when an item's text changes.
    }

    // Reference to the ItemActionListener.
    private ItemActionListener itemActionListener;

    // Setter for the ItemActionListener.
    public void setItemActionListener(ItemActionListener listener) {
        this.itemActionListener = listener;
    }

    // Constructor for the adapter.
    // Accepts the context (usually the hosting activity/fragment) and the initial list of items.
    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        // Initializing itemsList with a new copy of the provided list to avoid direct modification.
        this.itemsList = new ArrayList<>(items);
    }

    // This method inflates the layout for each list item and returns the corresponding ViewHolder.
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the list item layout from the resources.
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        // Creating and returning a new ViewHolder instance.
        return new ItemViewHolder(view);
    }

    // This method binds the data of a specific item to a given ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemsList.get(position);
        holder.bindData(item, position);
    }

    // Returns the total count of items in the list.
    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    // Method to update the list of items and notify the RecyclerView about the data change.
    public void updateData(List<Item> newData) {
        this.itemsList = newData;
        notifyDataSetChanged();
    }

    // Helper method to fetch an item from the list based on its position.
    public Item getItemAt(int position) {
        return itemsList.get(position);
    }

    // ViewHolder class that defines the view elements of each list item.
    public class ItemViewHolder extends RecyclerView.ViewHolder {

        // Views contained in each list item.
        EditText editTextItem;  // Editable text area for the item.
        ImageButton optionsButton,  // Button to show importance buttons.
                importantButton, normalButton,  unimportantButton; // Buttons to set importance.

        // Temporary storage to keep the text of the item when it first gains focus.
        // Used to compare against final text when focus is lost to detect changes.
        private String focusGainedOriginalText = null;

        // Constructor for the ItemViewHolder, initializing views and setting up listeners.
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding the views to their respective elements in the item layout.
            editTextItem = itemView.findViewById(R.id.editTextItem);
            optionsButton = itemView.findViewById(R.id.optionsButton);
            normalButton = itemView.findViewById(R.id.normalButton);
            unimportantButton = itemView.findViewById(R.id.unimportantButton);

            // TextWatcher for handling changes to the editTextItem.
            editTextItem.addTextChangedListener(new TextWatcher() {
                private boolean userTypedAtLeastOneChar = false;  // Flag to track if at least one character was typed.

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                // Handling text changes.
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Fetching the adapter position.
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Item currentItem = itemsList.get(position);
                        if (s.length() > 0) {
                            userTypedAtLeastOneChar = true;
                            // Checking for newline and handling it.
                            if (s.charAt(s.length() - 1) == '\n') {
                                editTextItem.setText(s.subSequence(0, s.length() - 1));
                                editTextItem.setSelection(s.length() - 1);
                                if (position != RecyclerView.NO_POSITION) {
                                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.NORMAL);
                                }
                                return;
                            }
                            currentItem.setText(s.toString());
                            if (!currentItem.isOptionsExpanded()) {
                                optionsButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Handling case where text is removed.
                            if (userTypedAtLeastOneChar) {
                                if (position != RecyclerView.NO_POSITION && position < itemsList.size() - 1) {
                                    if (itemActionListener != null) {
                                        itemActionListener.onItemRemove(position);
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Listener to detect focus changes on editTextItem. By noting text at focus gain and comparing it on focus loss,
            // we try to capture significant edits (e.g., 'red pepper' to 'green pepper') rather than every step.
            editTextItem.setOnFocusChangeListener((v, hasFocus) -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Item currentItem = itemsList.get(position);
                    if (hasFocus) {
                        // Store the current text on focus gain to serve as a baseline for comparison.
                        focusGainedOriginalText = currentItem.getText();
                    } else {
                        // Compare stored text with current text on focus loss to detect meaningful changes.
                        String currentText = editTextItem.getText().toString();
                        if (focusGainedOriginalText != null && !focusGainedOriginalText.equals(currentText)) {
                            // Notify the listener of the text change for potential undo actions.
                            itemActionListener.onItemTextChanged(currentItem, focusGainedOriginalText);
                        }
                        focusGainedOriginalText = null;  // Reset the stored text.
                    }
                }
            });

            // Listener to handle option button clicks.
            optionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemActionListener.onSetItemOptionsExpanded(position, true);
                    }
                }
            });

            // Listeners for importance buttons.
            // These listeners collapse the options and set the importance level as specified.
            normalButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (itemActionListener != null && position != RecyclerView.NO_POSITION) {
                    itemActionListener.onSetItemOptionsExpanded(position, false);
                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.NORMAL);
                }
            });
            unimportantButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (itemActionListener != null && position != RecyclerView.NO_POSITION) {
                    itemActionListener.onSetItemOptionsExpanded(position, false);
                    itemActionListener.onItemImportanceChange(position, Item.ImportanceLevel.UNIMPORTANT);
                }
            });
        }

        public void bindData(Item item, int position) {
            // Set the item's text to the editTextItem.
            editTextItem.setText(item.getText());

            // If the item is a new entry, focus on it and show the soft keyboard.
            if(item.isNewEntry()) {
                editTextItem.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextItem, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            // Toggle visibility of option buttons based on whether options are expanded.
            if (item.isOptionsExpanded()) {
                normalButton.setVisibility(View.VISIBLE);
                unimportantButton.setVisibility(View.VISIBLE);
                optionsButton.setVisibility(View.GONE);
            } else {
                normalButton.setVisibility(View.GONE);
                unimportantButton.setVisibility(View.GONE);
                // Hide options button if text is empty, otherwise show it.
                optionsButton.setVisibility(item.getText().isEmpty() ? View.GONE : View.VISIBLE);
            }

            int colorRes;

            // Determine the item background color based on its importance level and position (even/odd).
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

            // Set the calculated background color to the item's view.
            itemView.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        }
    }
}
