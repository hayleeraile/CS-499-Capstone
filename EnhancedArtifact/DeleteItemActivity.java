package com.zybooks.projecttwo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DeleteItemActivity extends AppCompatActivity {
    private Database dbHelper;
    private long itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        dbHelper = new Database(this);

        //retrieves the item id that was selected from the previous activity
        itemId = getIntent().getLongExtra("ITEM_ID", -1);

        Button buttonNo = findViewById(R.id.buttonNo);
        Button buttonYes = findViewById(R.id.buttonYes);


        buttonNo.setOnClickListener(v -> finish());
        buttonYes.setOnClickListener(v -> {
            //if the valid id was provided, then attempt deletion
            if (itemId != -1) {
                int rows = dbHelper.deleteItem(itemId);
                if (rows > 0) {
                    Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(this, getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        });
    }
}
