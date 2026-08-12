package com.zybooks.projecttwo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.DateFormat;
import java.util.Date;

//Displays one items details, allows edits, and shows the items history
public class ItemDetailsActivity  extends AppCompatActivity {
    private Database dbHelper;
    private long itemId;
    //loads the item and saves the original values for later comparison
    private String originalItemNum;
    private String originalDescription;
    private String originalLocation;
    private int originalQuantity;
    private int originalMinStock;

    private EditText editItemNumber, editItemDescription, editItemLocation, editItemQty, editStockLevel;
    private TextView itemHistory;

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
        itemHistory = findViewById(R.id.itemHistory);

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
        loadItemHistory();

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
            originalItemNum = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_NUMBER));
            originalDescription = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_DESC));
            originalLocation = cursor.getString(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_LOC));
            originalQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_QTY));
            originalMinStock = cursor.getInt(cursor.getColumnIndexOrThrow(Database.ItemDatabase.COL_ITEM_MIN_STOCK));

            editItemNumber.setText(originalItemNum);
            editItemDescription.setText(originalDescription);
            editItemLocation.setText(originalLocation);
            editItemQty.setText(String.valueOf(originalQuantity));
            editStockLevel.setText(String.valueOf(originalMinStock));

        }
        if (cursor != null) cursor.close();
    }

    private void loadItemHistory() {
        Cursor cursor = dbHelper.getItemHistory(itemId);

        //builds a history report before displaying it
        StringBuilder history = new StringBuilder();

        while (cursor.moveToNext()) {
            String action = cursor.getString(0);
            long timestamp = cursor.getLong(1);
            String username = cursor.getString(2);

            String formattedDate = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(timestamp));

            history.append(action).append(" by ")
                    .append(username).append("\n")
                    .append(formattedDate).append("\n\n");
        }

        if (history.length() == 0) {
            itemHistory.setText(getString(R.string.item_history));
        } else {
            itemHistory.setText(history.toString());
        }
        cursor.close();
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

        //prevents duplicate history entries when nothing is changed
        boolean itemChanged = !itemNumber.equals(originalItemNum) ||
                !itemDesc.equals(originalDescription) ||
                !itemLoc.equals(originalLocation) ||
                qty != originalQuantity || min != originalMinStock;

        if (!itemChanged) {
            Toast.makeText(this, getString(R.string.no_changes), Toast.LENGTH_SHORT).show();
            return;
        }

        int rows = dbHelper.updateItem(itemId, itemNumber, itemDesc, itemLoc, qty, min);

        if (rows > 0) {
            //records the successful update user the logged in user
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            long userId = prefs.getLong("current_user_id", -1);

            if (userId != -1) {
                dbHelper.addItemHistory(itemId, userId, "ITEM_UPDATED");
            }
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
