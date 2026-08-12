package com.zybooks.projecttwo;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

//Displays the inventory and allows users to search, filter, sort items and navigate the app.
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

    private SwitchMaterial lowStockSwitch;
    private Spinner sortingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbHelper = new Database(this);

        initializeViews();
        setupRecyclerView();
        setupSortingSpinner();
        setupItemListeners();
        setupNavigationListeners();
    }

    //reloads the inventory whenever the user returns to the screen.
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    //initializes all interface controls
    private void initializeViews() {
        textEmpty = findViewById(R.id.textEmptyState);
        editItemSearch = findViewById(R.id.editItemSearch);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonSMS = findViewById(R.id.buttonSMS);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        recyclerView = findViewById(R.id.inventoryGrid);
        lowStockSwitch = findViewById(R.id.lowStockSwitch);
        sortingSpinner = findViewById(R.id.sortingSpinner);
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
        lowStockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
    }

    private void setupSortingSpinner() {

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortingSpinner.setAdapter(spinnerAdapter);

        sortingSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        applyFilters();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
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
                        (dialog, which) -> deleteItem(item))
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show();
    }

    private void deleteItem(InventoryItem item) {
        int rowsDeleted = dbHelper.deleteItem(item.getId());

        if (rowsDeleted > 0) {
            //removes the deleted item from complete inventory list
            allItems.removeIf(currentItem -> currentItem.getId() == item.getId());

            //reapplies current filters and sorting after item has been deleted
            applyFilters();

            Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationListeners() {
        buttonAddItem.setOnClickListener(v ->
                openActivity(AddItemActivity.class));

        buttonSMS.setOnClickListener(v ->
                openActivity(PermissionsActivity.class));

        buttonSearch.setOnClickListener(v -> applyFilters());
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

    //stores every inventory item retrieved from the item database
    private final List<InventoryItem> allItems = new ArrayList<>();

    //stores only the items currently matching the search and filter options
    private final List<InventoryItem> displayedItems = new ArrayList<>();

    private void loadItems() {
        //this function reloads the master inventory list
        //also reapplies the current search/filter/sort options
        allItems.clear();
        allItems.addAll(getInventoryItems());

        applyFilters();
    }

    private enum SortOption {
        ITEM_NUMBER_ASC, ITEM_NUMBER_DESC, QUANTITY, LOCATION
    }

    private SortOption getSelectedSortOption() {
        return SortOption.values()[sortingSpinner.getSelectedItemPosition()];
    }

    //sorts the filtered inventory list based on the option the user selects
    private void sortDisplayedItems() {
        switch (getSelectedSortOption()) {
            case ITEM_NUMBER_ASC:
                displayedItems.sort((a, b) ->
                        a.getItemNumber().compareToIgnoreCase(b.getItemNumber()));
                break;

            case ITEM_NUMBER_DESC:
                displayedItems.sort((a, b) ->
                        b.getItemNumber().compareToIgnoreCase(a.getItemNumber()));
                break;

            case QUANTITY:
                displayedItems.sort((a, b) ->
                        Integer.compare(a.getQuantity(), b.getQuantity()));
                break;

            case LOCATION:
                displayedItems.sort((a, b) ->
                        a.getLocation().compareToIgnoreCase(b.getLocation()));
                break;
        }
    }

    //applies the current search/low-stock filter or sort option before updating recycler view
    private void applyFilters() {
        String search = editItemSearch.getText().toString().trim().toLowerCase();
        boolean showLowStockOnly = lowStockSwitch.isChecked();

        displayedItems.clear();

        //looks through the inventory items based on current filters
        for (InventoryItem item : allItems) {
            String itemNumber = item.getItemNumber().toLowerCase();
            String description = item.getDescription().toLowerCase();
            String location = item.getLocation().toLowerCase();

            boolean matchesSearch =
                    search.isEmpty() || itemNumber.contains(search) || description.contains(search)
                    || location.contains(search);

            boolean matchesLowStockFilter =
                    !showLowStockOnly || item.getQuantity() <= item.getMinimumStock();

            //if the item matches search and filter options, add it to display list
            if (matchesSearch && matchesLowStockFilter) {
                displayedItems.add(item);
            }
        }

        //applies sort order before displaying items
        sortDisplayedItems();

        adapter.setItems(new ArrayList<>(displayedItems));
        updateEmptyState();
    }

    //retrieves all inventory records and converts each into an inventory object
    private List<InventoryItem> getInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();

        Cursor cursor = dbHelper.getAllItems();

        if (cursor == null) {
            return items;
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_ID));
            String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_NUMBER));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_DESC));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_LOC));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_QTY));
            int minStock = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_MIN_STOCK));

            items.add(new InventoryItem(id, itemNumber, description, location, qty, minStock));
        }
        cursor.close();
        return items;
    }
}
