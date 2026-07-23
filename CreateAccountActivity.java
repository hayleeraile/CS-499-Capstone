package com.zybooks.projecttwo;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
    //database variable
    private Database dbHelper;
    //edit text variables
    private EditText editFirst, editLast, editEmail, editPhone, editUser, editPass, editConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //initializes the database helper method
        dbHelper = new Database(this);
        //setting the variables to the id from activity_create_account
        editFirst = findViewById(R.id.editFirstName);
        editLast  = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editUser = findViewById(R.id.editUserName);
        editPass = findViewById(R.id.editPassword);
        editConfirmation = findViewById(R.id.editConfirmation);
        //initializing the create button and once its clicked it takes the user back to login page
        Button buttonCreate = findViewById(R.id.buttonCreateAccount);
        Button buttonGoBack = findViewById(R.id.goBackButton);
        buttonCreate.setOnClickListener(v -> createAccount());
        //added a back to login page button
        buttonGoBack.setOnClickListener(v -> finish());
    }
    //create account method that takes user input and adds it to the database.
    private void createAccount() {
        //takes user input and puts it to a string
        String firstName = editFirst.getText().toString();
        String lastName = editLast.getText().toString();
        String email = editEmail.getText().toString();
        String phone = editPhone.getText().toString();
        String username = editUser.getText().toString();
        String password = editPass.getText().toString();
        String confirmation = editConfirmation.getText().toString();
        //if these fields are empty, a message is printed
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()
        || confirmation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        //if passwords do not match a message is printed
        if (!password.equals(confirmation)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        // takes the information and used the createUser method to create the user in teh database
        boolean created = dbHelper.createUser(firstName, lastName, email, phone, username, password);
        //if all requirements are met, an account created message is printed and it goes back to the login screen
        if (created) {
            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
            finish();
            //else, an error message is printed
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
    }
}
