package com.example.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import android.graphics.Color;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {


    // Define constants for each state of item.
    private static final int INITIAL_STATE = 0;
    private static final int ADDED_STATE = 1;
    private static final int OPTIONS_STATE = 2;
    private static final int IMPORTANT_STATE = 3;
    private static final int UNIMPORTANT_STATE = 4;

    private static final int COLOR_BLUE = Color.parseColor("#ADD8E6");
    private static final int COLOR_ORANGE = Color.parseColor("#FFA500");
    private static final int COLOR_PURPLE = Color.parseColor("#800080");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add the initial item to the list
        addItem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*SharedPreferences sharedPreferences = getSharedPreferences("ShoppingList", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Assuming items is an ArrayList<String> holding your shopping list items
        Set<String> set = new HashSet<>(items);
        editor.putStringSet("items", set);
        editor.apply();*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*SharedPreferences sharedPreferences = getSharedPreferences("ShoppingList", MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet("items", null);

        if (set != null) {
            // Clearing the items list before loading from SharedPreferences to avoid duplicates
            items.clear();
            items.addAll(set);
        }*/

        // You'll need to refresh your UI (ListView, RecyclerView, etc.) to reflect the loaded items
    }

    public void addItem() {
        // Get the main layout where we'll add items
        LinearLayout mainLayout = findViewById(R.id.mainLayout);

        // Inflate the list item layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newItemView = inflater.inflate(R.layout.list_item, null);

        // Initialize the views
        EditText editText = newItemView.findViewById(R.id.editTextItem);
        ImageButton addButton = newItemView.findViewById(R.id.addButton);
        ImageButton optionsButton = newItemView.findViewById(R.id.optionsButton);
        ImageButton importantButton = newItemView.findViewById(R.id.importantButton);
        ImageButton normalButton = newItemView.findViewById(R.id.normalButton);
        ImageButton unimportantButton = newItemView.findViewById(R.id.unimportantButton);
        ImageButton removeButton = newItemView.findViewById(R.id.removeButton);

        // Initially, set the item's state
        newItemView.setTag(INITIAL_STATE);
        updateUIForState(newItemView, INITIAL_STATE);

        // Add the new item to the main layout
        mainLayout.addView(newItemView);

        // Set click listeners for each button

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newItemView.setTag(ADDED_STATE);
                updateUIForState(newItemView, ADDED_STATE);
                addItem(); // This will add a new item each time the button is clicked
            }
        });

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newItemView.setTag(OPTIONS_STATE);
                updateUIForState(newItemView, OPTIONS_STATE);
            }
        });

        importantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newItemView.setTag(IMPORTANT_STATE);
                newItemView.setBackgroundColor(COLOR_ORANGE);
                updateUIForState(newItemView, (int) newItemView.getTag());
                reorderItems();
            }
        });

        normalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newItemView.setTag(ADDED_STATE);
                newItemView.setBackgroundColor(COLOR_BLUE);
                updateUIForState(newItemView, (int) newItemView.getTag());
                reorderItems();
            }
        });

        unimportantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newItemView.setTag(UNIMPORTANT_STATE);
                newItemView.setBackgroundColor(COLOR_PURPLE);
                updateUIForState(newItemView, (int) newItemView.getTag());
                reorderItems();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.removeView(newItemView);
            }
        });
    }

    private void updateUIForState(View itemView, int state) {

        // Initialize the views
        EditText editText = itemView.findViewById(R.id.editTextItem);
        ImageButton addButton = itemView.findViewById(R.id.addButton);
        ImageButton optionsButton = itemView.findViewById(R.id.optionsButton);
        ImageButton importantButton = itemView.findViewById(R.id.importantButton);
        ImageButton normalButton = itemView.findViewById(R.id.normalButton);
        ImageButton unimportantButton = itemView.findViewById(R.id.unimportantButton);
        ImageButton removeButton = itemView.findViewById(R.id.removeButton);

        switch (state) {
            case INITIAL_STATE:
                addButton.setVisibility(View.VISIBLE);
                optionsButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.GONE);
                importantButton.setVisibility(View.GONE);
                normalButton.setVisibility(View.GONE);
                unimportantButton.setVisibility(View.GONE);
                break;
            case ADDED_STATE:
            case IMPORTANT_STATE:
            case UNIMPORTANT_STATE:
                addButton.setVisibility(View.GONE);
                optionsButton.setVisibility(View.VISIBLE);
                removeButton.setVisibility(View.VISIBLE);
                importantButton.setVisibility(View.GONE);
                normalButton.setVisibility(View.GONE);
                unimportantButton.setVisibility(View.GONE);
                break;
            case OPTIONS_STATE:
                addButton.setVisibility(View.GONE);
                optionsButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.VISIBLE);
                importantButton.setVisibility(View.VISIBLE);
                normalButton.setVisibility(View.VISIBLE);
                unimportantButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void reorderItems() {
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        List<View> allItems = new ArrayList<>();
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            allItems.add(mainLayout.getChildAt(i));
        }

        // Sort based on importance
        allItems.sort(new Comparator<View>() {
            @Override
            public int compare(View o1, View o2) {
                int state1 = (int) o1.getTag();
                int state2 = (int) o2.getTag();

                // For the important state, it should always be at the top
                if (state1 == IMPORTANT_STATE) return -1;
                if (state2 == IMPORTANT_STATE) return 1;

                // For the added state, it should be after important but before others
                if (state1 == ADDED_STATE) return -1;
                if (state2 == ADDED_STATE) return 1;

                // For the unimportant state, it should be after important and added
                if (state1 == UNIMPORTANT_STATE) return -1;
                if (state2 == UNIMPORTANT_STATE) return 1;

                // The initial state should always be at the bottom
                if (state1 == INITIAL_STATE) return 1;
                if (state2 == INITIAL_STATE) return -1;

                // If there are other states in the future, this line can handle them.
                // For now, it's kind of a fallback, but in your current setup it won't be reached.
                return Integer.compare(state1, state2);
            }
        });

        // Re-add views in sorted order
        mainLayout.removeAllViews();
        for (View item : allItems) {
            mainLayout.addView(item);
        }
    }

}