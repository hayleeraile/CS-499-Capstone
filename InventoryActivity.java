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
    //setting variables for the recyclerView, inventoryAdapter, textview, edit text, database and action button
    private TextView textEmpty;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private Database dbHelper;
    private EditText editItemSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        //finds the empty state text by id from activity_inventory
        textEmpty = findViewById(R.id.textEmptyState);

        dbHelper = new Database(this);
        //finds the variables by id from activity_inventory
        editItemSearch = findViewById(R.id.editItemSearch);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        Button buttonSMS = findViewById(R.id.buttonSMS);
        Button buttonProfile = findViewById(R.id.buttonProfile);
        FloatingActionButton buttonAddItem = findViewById(R.id.buttonAddItem);
        recyclerView = findViewById(R.id.inventoryGrid);

        //https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/GridLayoutManager learned about vertical grids. this specific one creates a 2 column grid.
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        //puts the grid layout into the recycle view grid that we created in activity_inventory.xml
        recyclerView.setLayoutManager(layoutManager);

        // creates a new adapter for the recycler view
        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);

        //sets the onclick listener for when an inventory item is clicked to take it to the details screen
        adapter.setOnItemClickListener(item -> {
            Intent i = new Intent(InventoryActivity.this, ItemDetailsActivity.class);
            i.putExtra("ITEM_ID", item.getId());
            startActivity(i);
        });

        //when the delete button on a card is clicked, it deletes the item after the user confirms with the pop up.
        adapter.setOnDeleteClickListener((item, position) -> {
            //alert dialog creates a pop up box with confirm or cancel message
            new AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete item " + item.getItemNumber() + "?")
                    .setPositiveButton("Delete Item", (dialog, which) -> {
                        int rowsDeleted = dbHelper.deleteItem(item.getId());
                        if (rowsDeleted > 0) {
                            adapter.removeItem(position);
                            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Item could not be deleted. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        //sets onclick listeners for the buttons to tell them where to go
        buttonAddItem.setOnClickListener(v ->
                startActivity(new Intent(InventoryActivity.this, AddItemActivity.class)));

        buttonSMS.setOnClickListener(v ->
                startActivity(new Intent(InventoryActivity.this, PermissionsActivity.class)));

        buttonSearch.setOnClickListener(v -> loadItems());
        buttonProfile.setOnClickListener(v ->
                startActivity(new Intent(InventoryActivity.this, UserProfileActivity.class)));

        loadItems();
    }

    //on resume load the items to the screen
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    //load items method that populates the items from the database.
    private void loadItems() {
        List<InventoryItemActivity> items = new ArrayList<>();
        String search = editItemSearch.getText().toString();

        Cursor cursor = dbHelper.getAllItems();
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_ID));
            String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_NUMBER));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_DESC));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_LOC));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_QTY));
            int minStock = cursor.getInt(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_MIN_STOCK));

            // search bar
            if (!search.isEmpty() && !itemNumber.toLowerCase().contains(search.toLowerCase())) {
                continue;
            }

            items.add(new InventoryItemActivity(id, itemNumber, description, location, qty, minStock));
        }
        cursor.close();

        adapter.setItems(items);
        //if there are no items it displays the empty text state that says no items press + to add.
        if(items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        //if there are items, that message is hidden
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }
}