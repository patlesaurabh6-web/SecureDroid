package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class AlertDetailsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnRestrictApp, btnIgnore;
    private TextView txtAppName, txtPermission, txtTime, txtReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        btnBack = findViewById(R.id.btnBack);
        btnRestrictApp = findViewById(R.id.btnRestrictApp);
        btnIgnore = findViewById(R.id.btnIgnore);

        txtAppName = findViewById(R.id.txtAppName);
        txtPermission = findViewById(R.id.txtPermission);
        txtTime = findViewById(R.id.txtTime);
        txtReason = findViewById(R.id.txtReason);

        if (getIntent() != null) {
            String app = getIntent().getStringExtra("APP_NAME");
            String perm = getIntent().getStringExtra("PERMISSION");
            if (app != null) txtAppName.setText(app);
            if (perm != null) txtPermission.setText(perm);
        }

        btnBack.setOnClickListener(v -> finish());
        btnIgnore.setOnClickListener(v -> finish());

        btnRestrictApp.setOnClickListener(v -> showBlockAppDialog());
    }

    private void showBlockAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Block this App?");
        builder.setMessage("This app will be restricted and won't be able to access sensitive data.");
        builder.setPositiveButton("BLOCK", (dialog, which) -> {
            Toast.makeText(this, "App restricted successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
