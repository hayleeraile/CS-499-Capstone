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
    private Database dbHelper;
    private long itemId;
    private EditText editItemNumber, editItemDescription, editItemLocation, editItemQty, editStockLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        dbHelper = new Database(this);

        editItemNumber = findViewById(R.id.editItemNumber);
        editItemDescription = findViewById(R.id.editItemDescription);
        editItemLocation = findViewById(R.id.editItemLocation);
        editItemQty = findViewById(R.id.editItemQuantity);
        editStockLevel = findViewById(R.id.editStockLevel);

        Button buttonSaveChanges = findViewById(R.id.buttonSaveItemChanges);
        Button buttonDeleteItem = findViewById(R.id.buttonDeleteItem);
        Button buttonGoBack = findViewById(R.id.buttonGoBack);

        itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId == -1) {
            Toast.makeText(this, getString(R.string.item_select_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadItem();

        buttonSaveChanges.setOnClickListener(v -> saveChanges());
        buttonDeleteItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeleteItemActivity.class);
            intent.putExtra("ITEM_ID", itemId);
            startActivity(intent);
            finish();
        });
        buttonGoBack.setOnClickListener(v -> finish());
    }

    private void loadItem() {
        Cursor cursor = dbHelper.getItemById(itemId);
        if(cursor != null && cursor.moveToFirst()) {
            String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_NUMBER));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_DESC));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_LOC));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_QTY));
            int min = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_MIN_STOCK));

            editItemNumber.setText(itemNumber);
            editItemDescription.setText(description);
            editItemLocation.setText(location);
            editItemQty.setText(String.valueOf(qty));
            editStockLevel.setText(String.valueOf(min));

        }
        if (cursor != null) cursor.close();
    }

    private void saveChanges() {
        //Trims the input so that a space is not counted as a valid value.
        String itemNumber = editItemNumber.getText().toString().trim();
        String itemDesc = editItemDescription.getText().toString().trim();
        String itemLoc = editItemLocation.getText().toString().trim();
        String qtyStr = editItemQty.getText().toString().trim();
        String minStr = editStockLevel.getText().toString().trim();

        if (!validateRequiredItemFields(itemNumber, qtyStr, minStr)) {
            return;
        }

        int qty;
        int min;

        //try catch method prevents invalid numeric input
        try {
            qty = Integer.parseInt(qtyStr);
            min = Integer.parseInt(minStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.integer_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //inventory and min stock values should not be negative
        if (qty < 0) {
            editItemQty.setError(getString(R.string.negQuantity_error));
            editItemQty.requestFocus();
            return;
        }

        if (min < 0) {
            editStockLevel.setError(getString(R.string.negStockLevel_error));
            editStockLevel.requestFocus();
            return;
        }

        int rows = dbHelper.updateItem(itemId, itemNumber, itemDesc, itemLoc, qty, min);

        if(rows > 0) {
            Toast.makeText(this, getString(R.string.item_updated), Toast.LENGTH_SHORT).show();

            if (qty <= min) {
                showLowStockSMS(itemNumber, qty, min);
            }

            finish();

        } else {
            Toast.makeText(this, getString(R.string.update_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateRequiredItemFields( String itemNumber, String qtyStr, String minStr) {
        if (itemNumber.isEmpty()) {
            editItemNumber.setError(getString(R.string.number_required));
            editItemNumber.requestFocus();
            return false;
        }

        if (qtyStr.isEmpty()) {
            editItemQty.setError(getString(R.string.quantity_required));
            editItemQty.requestFocus();
            return false;
        }

        if (minStr.isEmpty()) {
            editStockLevel.setError(getString(R.string.minStock_required));
            editStockLevel.requestFocus();
            return false;
        }
        return true;
    }

    private void showLowStockSMS(String itemNumber, int qty, int min) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

        //if permissions are not enabled, don't attempt to alert the user.
        if (!smsEnabled) {
            return;
        }

        //check if the app has permissions to send alerts through SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.sms_notGranted), Toast.LENGTH_LONG).show();
            return;
        }

        //Toast simulates what an SMS would look like instead of depending on the emulator.
        String fakeSMS = getString(R.string.low_inventory_message, itemNumber, qty, min);
        Toast.makeText(this, fakeSMS, Toast.LENGTH_LONG).show();
    }

}
