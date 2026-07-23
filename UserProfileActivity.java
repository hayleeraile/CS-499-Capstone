package com.zybooks.projecttwo;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {
    //variables created for the database, current username, the text view and edit texts
    private Database dbHelper;
    private String currentUsername;
    private TextView textUsername;
    private EditText editFirstName, editLastName, editEmail, editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //initializes the database helper
        dbHelper = new Database(this);

        //gets the username that was saved during login. this helps find the user that is logged ins profile
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("current_username", "");

        //sets variables to the ids from activity_user_profile
        textUsername = findViewById(R.id.textUsername);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        //initializes buttons from ids from activity_user_profile
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        // Load user data
        loadUserData();

        //sets onclick listeners for the save and cancel buttons
        buttonSave.setOnClickListener(v -> saveChanges());
        buttonCancel.setOnClickListener(v -> finish());
    }

    //loads the user data based on the username
    private void loadUserData() {

        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                //gets each variable by accessing the database and each column
                String username = cursor.getString(cursor.getColumnIndexOrThrow(Database.userDatabase.COL_USERNAME));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(Database.userDatabase.COL_USER_FIRST));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(Database.userDatabase.COL_USER_LAST));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(Database.userDatabase.COL_USER_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(Database.userDatabase.COL_USER_PHONE));

                //sets the text field of each line with the variable
                textUsername.setText(username);
                editFirstName.setText(firstName);
                editLastName.setText(lastName);
                editEmail.setText(email);
                editPhone.setText(phone);
            } else {
                Toast.makeText(this, "User was not found.", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }

    //if the user updates their data this method updates the database
    private void saveChanges() {
        //collects the user input and puts it to a string
        String firstName = editFirstName.getText().toString();
        String lastName = editLastName.getText().toString();
        String email = editEmail.getText().toString();
        String phone = editPhone.getText().toString();

        //if these fields are empty, an error message is printed
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //writes to the database to update the user
        boolean updated = dbHelper.updateUser(currentUsername, firstName, lastName, email, phone);

        //if the user was updated, a confirmation message is printed adn the screen is closed
        if (updated) {
            Toast.makeText(this, "Profile has been updated", Toast.LENGTH_SHORT).show();
            finish();
            //else, an error message is printed
        } else {
            Toast.makeText(this, "There was an error updating the profile", Toast.LENGTH_SHORT).show();
        }
    }
}