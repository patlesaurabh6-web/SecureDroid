package com.example.securedroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;
import com.example.securedroid.activities.dashboard.DashboardActivity;

public class PermissionSetupActivity extends AppCompatActivity {

    private Button btnGrantAccessibility, btnGrantNotification, btnGrantVpn, btnGrantUsage, btnGrantOverlay, btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_setup);

        btnGrantAccessibility = findViewById(R.id.btnGrantAccessibility);
        btnGrantNotification = findViewById(R.id.btnGrantNotification);
        btnGrantVpn = findViewById(R.id.btnGrantVpn);
        btnGrantUsage = findViewById(R.id.btnGrantUsage);
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay);
        btnContinue = findViewById(R.id.btnContinue);

        setupClickListeners();
    }

    private void setupClickListeners() {
        btnGrantAccessibility.setOnClickListener(v -> {
            btnGrantAccessibility.setText("GRANTED");
            btnGrantAccessibility.setAlpha(0.6f);
            Toast.makeText(this, "Accessibility Permission Granted", Toast.LENGTH_SHORT).show();
        });

        btnGrantNotification.setOnClickListener(v -> {
            btnGrantNotification.setText("GRANTED");
            btnGrantNotification.setAlpha(0.6f);
            Toast.makeText(this, "Notification Access Granted", Toast.LENGTH_SHORT).show();
        });

        btnGrantVpn.setOnClickListener(v -> {
            btnGrantVpn.setText("GRANTED");
            btnGrantVpn.setAlpha(0.6f);
            Toast.makeText(this, "VPN Permission Granted", Toast.LENGTH_SHORT).show();
        });

        btnGrantUsage.setOnClickListener(v -> {
            btnGrantUsage.setText("GRANTED");
            btnGrantUsage.setAlpha(0.6f);
            Toast.makeText(this, "Usage Access Granted", Toast.LENGTH_SHORT).show();
        });

        btnGrantOverlay.setOnClickListener(v -> {
            btnGrantOverlay.setText("GRANTED");
            btnGrantOverlay.setAlpha(0.6f);
            Toast.makeText(this, "Overlay Permission Granted", Toast.LENGTH_SHORT).show();
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(PermissionSetupActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
