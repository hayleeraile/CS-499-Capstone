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

        SwitchCompat toggleSwitch = findViewById(R.id.enableSwitch);
        Button buttonCheckPermission = findViewById(R.id.buttonCheckPermission);
        Button buttonGoBack = findViewById(R.id.buttonGoBack);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", false);


        toggleSwitch.setChecked(smsEnabled);

        toggleSwitch.setOnCheckedChangeListener(((buttonView,
                                                  isChecked) -> {
            //saves the alert preference of the user so it stays enabled or disabled
            prefs.edit().putBoolean("sms_enabled", isChecked).apply();
            String msg = isChecked ? getString(R.string.sms_enabled) :
                    getString(R.string.sms_disabled);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));

        buttonCheckPermission.setOnClickListener(v -> checkSmsPermission());
        buttonGoBack.setOnClickListener(v -> finish());
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.sms_already_granted),
                    Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, REQ_SEND_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_SEND_SMS) {
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.sms_granted),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.sms_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
