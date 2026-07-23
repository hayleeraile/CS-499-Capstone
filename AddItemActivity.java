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
    //database variable
    private Database dbHelper;
    //edit text variables
    private EditText editItemNumber, editItemDesc, editItemLocation, editItemQty, editItemStockLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //initialize db helper method
        dbHelper = new Database(this);
        //linking our variables to the id from the activity_add.xml file
        editItemNumber = findViewById(R.id.editItemNumber);
        editItemDesc = findViewById(R.id.editItemDescription);
        editItemLocation = findViewById(R.id.editItemLocation);
        editItemQty = findViewById(R.id.editItemQuantity);
        editItemStockLevel = findViewById(R.id.editItemStockLevel);
        //initializing our buttons and setting on click listeners
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);
        //if the cancel button is clicked it closes the tab and goes to previous page
        buttonCancel.setOnClickListener(v -> finish());
        //if add item is clicked, it triggers the save item method
        buttonAddItem.setOnClickListener(v -> saveItem());
    }
    //save item method that takes the text and saves them.
    private void saveItem() {
        //collects the user input and puts it to a string
        String itemNumber = editItemNumber.getText().toString();
        String itemDescription = editItemDesc.getText().toString();
        String itemLocation = editItemLocation.getText().toString();
        String itemQty = editItemQty.getText().toString();
        String itemStockLevel = editItemStockLevel.getText().toString();
        //if any of those items are empty, a message will be displayed saying that they are required.
        if (itemNumber.isEmpty() || itemQty.isEmpty() || itemStockLevel.isEmpty()) {
            Toast.makeText(this, "Item number, quantity and stock levels must be filled out", Toast.LENGTH_SHORT).show();
            return;
        }

        //changes these items back into a integer
        int qty = Integer.parseInt(itemQty);
        int min = Integer.parseInt(itemStockLevel);

        //adds the item into the add item method from the database file
        long id = dbHelper.addItem(itemNumber, itemDescription, itemLocation, qty, min);
        //if everything goes well a confirmation message is printed and it goes back to the inventory grid screen
        if (id != -1) {
            if (qty <= min) {
                showLowStockSMS(itemNumber, qty, min);
            }

            Toast.makeText(this, "Item was added", Toast.LENGTH_SHORT).show();
            finish();
            //else, an error message is printed
        } else {
            Toast.makeText(this, "There was an error adding item", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "SMS permissions were not granted, alert was triggered but not sent!", Toast.LENGTH_LONG).show();
            return;
        }
        //low stock alert message
        //I chose to use a Toast message simulation so that I didn't have to rely on the emulator.
        String fakeSMS = "Low inventory alert: Item " + itemNumber + " is at quantity " + qty + " (min stock level is " + min + ").";
        Toast.makeText(this, fakeSMS, Toast.LENGTH_LONG).show();
    }
}
