package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class AppDetailsActivity extends AppCompatActivity {

    private TextView txtAppName, txtPackageName, txtAppRiskScore, txtAppSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        txtAppName = findViewById(R.id.txtAppName);
        txtPackageName = findViewById(R.id.txtPackageName);
        txtAppRiskScore = findViewById(R.id.txtAppRiskScore);
        txtAppSummary = findViewById(R.id.txtAppSummary);

        String appName = getIntent().getStringExtra("APP_NAME");
        String pkgName = getIntent().getStringExtra("PACKAGE_NAME");

        if (appName != null && txtAppName != null) txtAppName.setText(appName);
        if (pkgName != null && txtPackageName != null) txtPackageName.setText("Package: " + pkgName);
    }
}