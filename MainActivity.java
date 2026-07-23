package com.zybooks.projecttwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    //sets variables for edit text and database
    private EditText editUser, editPass;
    private Database dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializes database helper
        dbHelper = new Database(this);

        //sets variables equal to the ids from activity_main
        editUser = findViewById(R.id.editUserName);
        editPass = findViewById(R.id.editPassword);
        //initializes buttons by id
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreate = findViewById(R.id.buttonCreateAccount);

        //sets onclick listeners for each button
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateAccountActivity.class)));
    }

    //method for attempting to login
    private void attemptLogin() {
        //take user input and set variables
        String username = editUser.getText().toString();
        String password = editPass.getText().toString();

        //if these are empty a message is printed saying to fill them out
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        //if the user is validated, a confirmation message is printed and the inventory grid screen activates
        if (dbHelper.validateUser(username, password)) {
            //collect saved username
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putString("current_username", username).apply();
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, InventoryActivity.class));
        //else, an error message is printed
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}