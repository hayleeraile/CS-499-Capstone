package com.zybooks.projecttwo;


import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
    private Database dbHelper;
    private EditText editFirst, editLast, editEmail, editPhone, editUser, editPass, editConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        dbHelper = new Database(this);

        editFirst = findViewById(R.id.editFirstName);
        editLast  = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editUser = findViewById(R.id.editUserName);
        editPass = findViewById(R.id.editPassword);
        editConfirmation = findViewById(R.id.editConfirmation);

        Button buttonCreate = findViewById(R.id.buttonCreateAccount);
        Button buttonGoBack = findViewById(R.id.goBackButton);

        buttonCreate.setOnClickListener(v -> createAccount());
        buttonGoBack.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        //Trims the input so that a space is not counted as a valid value.
        String firstName = editFirst.getText().toString().trim();
        String lastName = editLast.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String username = editUser.getText().toString().trim();
        String password = editPass.getText().toString().trim();
        String confirmation = editConfirmation.getText().toString().trim();

        if (!validateUserAccountFields(firstName, lastName, email, phone, username, password, confirmation)) {
            return;
        }

        boolean created = dbHelper.createUser(firstName, lastName, email, phone, username, password);

        if (created) {
            Toast.makeText(this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.user_exists), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateUserAccountFields(String firstName,
                                          String lastName,
                                          String email,
                                          String phone,
                                          String username,
                                          String password,
                                          String confirmation) {
        if (firstName.isEmpty()) {
            editFirst.setError(getString(R.string.firstName_required));
            editFirst.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            editLast.setError(getString(R.string.lastName_required));
            editLast.requestFocus();
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

        if (username.isEmpty()) {
            editUser.setError(getString(R.string.user_required));
            editUser.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editPass.setError(getString(R.string.password_required));
            editPass.requestFocus();
            return false;
        }

        //requires password to be a minimum length
        if (password.length() < 8) {
            editPass.setError(getString(R.string.passLength_requirement));
            editPass.requestFocus();
            return false;
        }

        if (confirmation.isEmpty()) {
            editConfirmation.setError(getString(R.string.passConfirmation_required));
            editConfirmation.requestFocus();
            return false;
        }

        //requires the password and confirmation password to match
        if (!password.equals(confirmation)) {
            editConfirmation.setError(getString(R.string.passMatch_error));
            editConfirmation.requestFocus();
            return false;
        }
        return true;
    }
}
