package com.zybooks.projecttwo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {
    private TextView textEmpty;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private Database dbHelper;
    private EditText editItemSearch;

    private Button buttonSearch;
    private Button buttonSMS;
    private Button buttonProfile;
    private FloatingActionButton buttonAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbHelper = new Database(this);

        initializeViews();
        setupRecyclerView();
        setupItemListeners();
        setupNavigationListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void initializeViews() {
        textEmpty = findViewById(R.id.textEmptyState);
        editItemSearch = findViewById(R.id.editItemSearch);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonSMS = findViewById(R.id.buttonSMS);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        recyclerView = findViewById(R.id.inventoryGrid);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupItemListeners() {
        adapter.setOnItemClickListener(item -> openItemDetails(item.getId()));
        adapter.setOnDeleteClickListener(this::showDeleteConfirmation);
    }

    private void openItemDetails(long itemId) {
        Intent i = new Intent(this, ItemDetailsActivity.class);
        i.putExtra("ITEM_ID", itemId);
        startActivity(i);
    }

    private void showDeleteConfirmation(InventoryItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_message))
                .setMessage(getString(R.string.delete_item_confirmation, item.getItemNumber()))
                .setPositiveButton(getString(R.string.button_delete),
                        (dialog, which) -> deleteItem(item, position))
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show();
    }

    private void deleteItem(InventoryItem item, int position) {
        int rowsDeleted = dbHelper.deleteItem(item.getId());

        if (rowsDeleted > 0) {
            adapter.removeItem(position);
            updateEmptyState();
            Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.delete_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationListeners() {
        buttonAddItem.setOnClickListener(v ->
                openActivity(AddItemActivity.class));

        buttonSMS.setOnClickListener(v ->
                openActivity(PermissionsActivity.class));

        buttonSearch.setOnClickListener(v -> loadItems());
        buttonProfile.setOnClickListener(v ->
                openActivity(UserProfileActivity.class));
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;

        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadItems() {
        String search = editItemSearch.getText().toString().trim();
        List<InventoryItem> items = getInventoryItems();
        items = filterItems(items, search);
        adapter.setItems(items);
        updateEmptyState();
    }

    private List<InventoryItem> getInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();

        Cursor cursor = dbHelper.getAllItems();

        if (cursor == null) {
            return items;
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_ID));
            String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_NUMBER));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_LOC));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_QTY));

            items.add(new InventoryItem(id, itemNumber, location, qty));
        }
        cursor.close();
        return items;
    }

    private List<InventoryItem> filterItems(
            List<InventoryItem> items,
            String search) {
        if (search.isEmpty()) {
            return items;
        }

        List<InventoryItem> filteredItems = new ArrayList<>();

        for (InventoryItem item : items) {
            if (item.getItemNumber().toLowerCase().contains(search.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }
}