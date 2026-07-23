package com.zybooks.projecttwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private EditText editUser, editPass;
    private Database dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new Database(this);

        editUser = findViewById(R.id.editUserName);
        editPass = findViewById(R.id.editPassword);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreate = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonCreate.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreateAccountActivity.class)));
    }

    private void attemptLogin() {
        String username = editUser.getText().toString().trim();
        String password = editPass.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.user_pass_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.validateUser(username, password)) {
            //save the logged in username so the profile screen loads the proper user
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putString("current_username", username).apply();
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, InventoryActivity.class));
        } else {
            Toast.makeText(this, getString(R.string.invalid_user_pass), Toast.LENGTH_SHORT).show();
        }
    }
}