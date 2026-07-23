package com.zybooks.projecttwo;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsActivity  extends AppCompatActivity {
    private static final int REQ_SEND_SMS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        //initializes the views and buttons for check permission and go back
        SwitchCompat toggleSwitch = findViewById(R.id.enableSwitch);
        Button buttonCheckPermission = findViewById(R.id.buttonCheckPermission);
        Button buttonGoBack = findViewById(R.id.buttonGoBack);

        //loads the saved state for the toggle
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);

        //restores the previous selection
        toggleSwitch.setChecked(smsEnabled);
        //when the user enables or disables, a message is printed through the onclick listener
        toggleSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sms_enabled", isChecked).apply();
            String msg = isChecked ? "SMS alerts are enabled (if permissions granted)." : "SMS alerts are disabled.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
        //other buttons have onclick listeners set here
        buttonCheckPermission.setOnClickListener(v -> checkSmsPermission());
        buttonGoBack.setOnClickListener(v -> finish());
    }

    //checks that sms permission is granted. if it is not, the app will request permissions
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permissions are already granted", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQ_SEND_SMS);
        }
    }

    //this method displays where the permissions are granted or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_SEND_SMS) {
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permissions are granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permissions are denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
