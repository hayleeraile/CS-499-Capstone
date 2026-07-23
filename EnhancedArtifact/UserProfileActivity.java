package com.zybooks.projecttwo;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {
    private Database dbHelper;
    private String currentUsername;
    private TextView textUsername;
    private EditText editFirstName, editLastName, editEmail, editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbHelper = new Database(this);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("current_username", "");

        if (currentUsername.isEmpty()) {
            Toast.makeText(this,
                    getString(R.string.user_not_loggedIn),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textUsername = findViewById(R.id.textUsername);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        loadUserData();

        buttonSave.setOnClickListener(v -> saveChanges());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadUserData() {

        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(Database.UserDatabase.COL_USERNAME));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(Database.UserDatabase.COL_USER_FIRST));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(Database.UserDatabase.COL_USER_LAST));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(Database.UserDatabase.COL_USER_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(Database.UserDatabase.COL_USER_PHONE));

                textUsername.setText(username);
                editFirstName.setText(firstName);
                editLastName.setText(lastName);
                editEmail.setText(email);
                editPhone.setText(phone);
            } else {
                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                finish();
            }
            cursor.close();
        }
    }

    private void saveChanges() {
        //Trims the input so that a space is not counted as a valid value.
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (!validateUserAccountFields(firstName, lastName, email, phone)) {
            return;
        }

        boolean updated = dbHelper.updateUser(currentUsername, firstName, lastName, email, phone);

        if (updated) {
            Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.profile_update_error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateUserAccountFields(String firstName,
                                          String lastName,
                                          String email,
                                          String phone) {
        if (firstName.isEmpty()) {
            editFirstName.setError(getString(R.string.firstName_required));
            editFirstName.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            editLastName.setError(getString(R.string.lastName_required));
            editLastName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            editEmail.setError(getString(R.string.email_required));
            editEmail.requestFocus();
            return false;
        }

        //built in email pattern to check for valid email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.valid_email));
            editEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            editPhone.setError(getString(R.string.phone_required));
            editPhone.requestFocus();
            return false;
        }

        //removes format characters before checking for 10 digits
        String digitsOnly = phone.replaceAll("\\D", "");

        if (digitsOnly.length() != 10) {
            editPhone.setError(getString(R.string.valid_phone));
            editPhone.requestFocus();
            return false;
        }
        return true;
    }
}