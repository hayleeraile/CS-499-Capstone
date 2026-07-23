package com.zybooks.projecttwo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DeleteItemActivity extends AppCompatActivity {
    //database variable
    private Database dbHelper;
    //item id variable
    private long itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        //initializes the database helper
        dbHelper = new Database(this);

        //retrieves the item id that was selected from the previous activity
        itemId = getIntent().getLongExtra("ITEM_ID", -1);

        //initializes the buttons by id from activity_delete
        Button buttonNo = findViewById(R.id.buttonNo);
        Button buttonYes = findViewById(R.id.buttonYes);

        //sets onclick listeners for those buttons
        buttonNo.setOnClickListener(v -> finish());
        buttonYes.setOnClickListener(v -> {
            if (itemId != -1) {
                int rows = dbHelper.deleteItem(itemId);
                if (rows > 0) {
                    Toast.makeText(this, "Item has been deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(this, "There was an error deleting item", Toast.LENGTH_SHORT).show();
                }
            }
            //closes the screen
            finish();
        });
    }
}
