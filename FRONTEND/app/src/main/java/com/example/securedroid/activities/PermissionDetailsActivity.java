package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class PermissionDetailsActivity extends AppCompatActivity {

    private TextView txtPermTitle, txtPermCategory, txtPermRiskLevel, txtPermDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_details);

        txtPermTitle = findViewById(R.id.txtPermTitle);
        txtPermCategory = findViewById(R.id.txtPermCategory);
        txtPermRiskLevel = findViewById(R.id.txtPermRiskLevel);
        txtPermDesc = findViewById(R.id.txtPermDesc);

        String name = getIntent().getStringExtra("PERM_NAME");
        String category = getIntent().getStringExtra("PERM_CATEGORY");
        String risk = getIntent().getStringExtra("PERM_RISK");
        String desc = getIntent().getStringExtra("PERM_DESC");

        if (name != null && txtPermTitle != null) txtPermTitle.setText(name);
        if (category != null && txtPermCategory != null) txtPermCategory.setText("Category: " + category);
        if (risk != null && txtPermRiskLevel != null) txtPermRiskLevel.setText("Risk Level: " + risk);
        if (desc != null && txtPermDesc != null) txtPermDesc.setText(desc);
    }
}