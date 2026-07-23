package com.zybooks.projecttwo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {
    private Database dbHelper;
    private EditText editItemNumber, editItemDesc, editItemLocation, editItemQty, editItemStockLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        dbHelper = new Database(this);

        editItemNumber = findViewById(R.id.editItemNumber);
        editItemDesc = findViewById(R.id.editItemDescription);
        editItemLocation = findViewById(R.id.editItemLocation);
        editItemQty = findViewById(R.id.editItemQuantity);
        editItemStockLevel = findViewById(R.id.editItemStockLevel);

        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);

        buttonCancel.setOnClickListener(v -> finish());
        buttonAddItem.setOnClickListener(v -> saveItem());
    }
    private void saveItem() {
        //Trims the input so that a space is not counted as a valid value.
        String itemNumber = editItemNumber.getText().toString().trim();
        String itemDescription = editItemDesc.getText().toString().trim();
        String itemLocation = editItemLocation.getText().toString().trim();
        String itemQty = editItemQty.getText().toString().trim();
        String itemStockLevel = editItemStockLevel.getText().toString().trim();

        if (!validateRequiredItemFields(
                itemNumber, itemQty, itemStockLevel)) {
            return;
        }

        int qty;
        int min;

        //try catch method prevents invalid numeric input
        try {
            qty = Integer.parseInt(itemQty);
            min = Integer.parseInt(itemStockLevel);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.integer_error), Toast.LENGTH_SHORT).show();
            return;
        }

        //inventory and min stock values should not be negative
        if (qty < 0) {
            editItemQty.setError(getString(R.string.negQuantity_error));
            editItemQty.requestFocus();
            return;
        }
        if (min < 0) {
            editItemStockLevel.setError(getString(R.string.negStockLevel_error));
            editItemStockLevel.requestFocus();
            return;
        }


        long id = dbHelper.addItem(itemNumber, itemDescription, itemLocation, qty, min);

        if (id != -1) {
            if (qty <= min) {
                showLowStockSMS(itemNumber, qty, min);
            }

            Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.item_add_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateRequiredItemFields(
            String itemNumber, String itemQty, String itemStockLevel) {
        if (itemNumber.isEmpty()) {
            editItemNumber.setError(getString(R.string.number_required));
            editItemNumber.requestFocus();
            return false;
        }

        if (itemQty.isEmpty()) {
            editItemQty.setError(getString(R.string.quantity_required));
            editItemQty.requestFocus();
            return false;
        }

        if (itemStockLevel.isEmpty()) {
            editItemStockLevel.setError(getString(R.string.minStock_required));
            editItemStockLevel.requestFocus();
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
