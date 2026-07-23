package com.zybooks.projecttwo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ItemDetailsActivity  extends AppCompatActivity {
    //variables for database, itemId, and edit text
    private Database dbHelper;
    private long itemId;
    private EditText editItemNumber, editItemDescription, editItemLocation, editItemQty, editStockLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //initializes the database helper
        dbHelper = new Database(this);
        //sets variables to the ids from activity_details
        editItemNumber = findViewById(R.id.editItemNumber);
        editItemDescription = findViewById(R.id.editItemDescription);
        editItemLocation = findViewById(R.id.editItemLocation);
        editItemQty = findViewById(R.id.editItemQuantity);
        editStockLevel = findViewById(R.id.editStockLevel);
        //instantiates buttons
        Button buttonSaveChanges = findViewById(R.id.buttonSaveItemChanges);
        Button buttonDeleteItem = findViewById(R.id.buttonDeleteItem);
        //added a back button in case they wanted to go back
        Button buttonGoBack = findViewById(R.id.buttonGoBack);

        //The item ID is selected from the previous activity. this checks for validation that there was an ID passed
        //to be able to open the item details screen
        itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId == -1) {
            Toast.makeText(this, "No item has been selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadItem();
        //sets onclick listeners for the go back, save and delete buttons.
        buttonSaveChanges.setOnClickListener(v -> saveChanges());
        buttonDeleteItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeleteItemActivity.class);
            intent.putExtra("ITEM_ID", itemId); startActivity(intent);
            finish(); //go back to grid when item has confirmed deletion
        });
        buttonGoBack.setOnClickListener(v -> finish());
    }

    //load item method loads the item details from the database into the item details screen
    private void loadItem() {
        Cursor cursor = dbHelper.getItemById(itemId);
        if(cursor != null && cursor.moveToFirst()) {
            String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_NUMBER));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_DESC));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_LOC));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_QTY));
            int min = cursor.getInt(cursor.getColumnIndexOrThrow(Database.itemDatabase.COL_ITEM_MIN_STOCK));

            editItemNumber.setText(itemNumber);
            editItemDescription.setText(description);
            editItemLocation.setText(location);
            editItemQty.setText(String.valueOf(qty));
            editStockLevel.setText(String.valueOf(min));

        }
        if (cursor != null) cursor.close();
    }

    //if the user updates anything in the item details, this screen saves the updated information
    private void saveChanges() {
        String itemNumber = editItemNumber.getText().toString();
        String itemDesc = editItemDescription.getText().toString();
        String itemLoc = editItemLocation.getText().toString();
        String qtyStr = editItemQty.getText().toString();
        String minStr = editStockLevel.getText().toString();

        //if qty and stock level fields are empty, a message is printed
        if(qtyStr.isEmpty() || minStr.isEmpty()) {
            Toast.makeText(this, "quantity and min stock levels are required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        int min = Integer.parseInt(minStr);

        int rows = dbHelper.updateItem(itemId, itemNumber, itemDesc, itemLoc, qty, min);

        if(rows > 0) {
            Toast.makeText(this, "Item has been updated", Toast.LENGTH_SHORT).show();

            if (qty <= min) {
                showLowStockSMS(itemNumber, qty, min);                }

            finish();

        } else {
            Toast.makeText(this, "There was an error updating the item", Toast.LENGTH_SHORT).show();
        }
    }
    //sends a text to the user if their item qty is less than or equal to the min stock level
    private void showLowStockSMS(String itemNumber, int qty, int min) {
        //reads the saved preferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

        //if permissions are not enabled, just return, dont crash
        if (!smsEnabled) {
            return;
        }

        //check if the app has permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permissions were not granted, the alert was triggered but not sent!", Toast.LENGTH_LONG).show();
            return;
        }
        //low stock alert message

        //I chose to use a Toast message simulation so that I didn't have to rely on the emulator.
        String fakeSMS = "Low inventory alert: Item " + itemNumber + " is at quantity " + qty + " (min stock level is " + min + ").";
        Toast.makeText(this, fakeSMS, Toast.LENGTH_LONG).show();
    }

}
