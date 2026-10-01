package com.example.securedroid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;

public class AlertDetailsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnRestrictApp, btnIgnore;
    private TextView txtAlertTitle, txtAppName, txtPermission, txtTime, txtReason, txtAiRecommendation;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        btnBack = findViewById(R.id.btnBack);
        btnRestrictApp = findViewById(R.id.btnRestrictApp);
        btnIgnore = findViewById(R.id.btnIgnore);

        txtAlertTitle = findViewById(R.id.txtAlertTitle);
        txtAppName = findViewById(R.id.txtAppName);
        txtPermission = findViewById(R.id.txtPermission);
        txtTime = findViewById(R.id.txtTime);
        txtReason = findViewById(R.id.txtReason);
        txtAiRecommendation = findViewById(R.id.txtAiRecommendation);

        if (getIntent() != null) {
            String app = getIntent().getStringExtra("APP_NAME");
            packageName = getIntent().getStringExtra("PACKAGE_NAME");
            String perm = getIntent().getStringExtra("PERMISSION");
            String time = getIntent().getStringExtra("TIME");
            String reason = getIntent().getStringExtra("DESCRIPTION");
            String prevRisk = getIntent().getStringExtra("PREV_RISK");
            String currRisk = getIntent().getStringExtra("CURR_RISK");

            if (app != null && txtAppName != null) txtAppName.setText(app);
            if (perm != null && txtPermission != null) txtPermission.setText(perm);
            if (time != null && txtTime != null) txtTime.setText(time);
            if (reason != null && txtReason != null) txtReason.setText(reason);

            if (txtAlertTitle != null && app != null) {
                txtAlertTitle.setText(app + " - Privacy Audit");
            }

            if (txtAiRecommendation != null) {
                if (currRisk != null && currRisk.toUpperCase().contains("HIGH")) {
                    txtAiRecommendation.setText("We recommend reviewing or disabling sensitive permissions (Camera, Location, SMS) in Android Settings.");
                } else {
                    txtAiRecommendation.setText("Review background permission usage in App Settings to ensure compliance with your privacy preferences.");
                }
            }
        }

        btnBack.setOnClickListener(v -> finish());
        btnIgnore.setOnClickListener(v -> {
            Toast.makeText(this, "Alert marked as reviewed.", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnRestrictApp.setOnClickListener(v -> openAppSettings());
    }

    private void openAppSettings() {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Android Application Settings", Toast.LENGTH_SHORT).show();
        }
    }
}
