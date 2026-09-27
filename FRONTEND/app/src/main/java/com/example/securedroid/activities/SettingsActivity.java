package com.example.securedroid.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.activities.auth.LoginActivity;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.DashboardSummaryResponse;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBackSettings;
    private MaterialSwitch switchRealtime, switchAutoScan, switchAlerts;
    private View layoutServerIp, layoutClearCache, layoutPrivacyPolicy;
    private TextView txtCurrentServerIp;
    private MaterialButton btnTestConnection, btnLogoutSettings;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBackSettings = findViewById(R.id.btnBackSettings);
        switchRealtime = findViewById(R.id.switchRealtime);
        switchAutoScan = findViewById(R.id.switchAutoScan);
        switchAlerts = findViewById(R.id.switchAlerts);

        layoutServerIp = findViewById(R.id.layoutServerIp);
        txtCurrentServerIp = findViewById(R.id.txtCurrentServerIp);
        btnTestConnection = findViewById(R.id.btnTestConnection);

        layoutClearCache = findViewById(R.id.layoutClearCache);
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);
        btnLogoutSettings = findViewById(R.id.btnLogoutSettings);

        if (txtCurrentServerIp != null) {
            txtCurrentServerIp.setText(ApiClient.BASE_URL);
        }
    }

    private void setupListeners() {
        if (btnBackSettings != null) {
            btnBackSettings.setOnClickListener(v -> finish());
        }

        if (switchRealtime != null) {
            switchRealtime.setOnCheckedChangeListener((btn, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(this, "Real-time Privacy Shield " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (switchAutoScan != null) {
            switchAutoScan.setOnCheckedChangeListener((btn, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(this, "Auto-scan on app install " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (switchAlerts != null) {
            switchAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(this, "High-risk alerts " + status, Toast.LENGTH_SHORT).show();
            });
        }

        if (layoutServerIp != null) {
            layoutServerIp.setOnClickListener(v -> showServerIpDialog());
        }

        if (btnTestConnection != null) {
            btnTestConnection.setOnClickListener(v -> testBackendConnection());
        }

        if (layoutClearCache != null) {
            layoutClearCache.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear Scan Cache")
                        .setMessage("Are you sure you want to clear locally cached application scans?")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            sessionManager.saveLastScanTime(null);
                            Toast.makeText(this, "Scan cache cleared successfully.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (layoutPrivacyPolicy != null) {
            layoutPrivacyPolicy.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Privacy Policy & Terms")
                        .setMessage("SecureDroid is committed to protecting your digital privacy. All permission scanning is performed locally on your device, and sensitive permissions are evaluated using secure cryptographic channels.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        if (btnLogoutSettings != null) {
            btnLogoutSettings.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out of SecureDroid?")
                        .setPositiveButton("Log Out", (dialog, which) -> {
                            sessionManager.logout();
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void showServerIpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configure Server IP");

        final EditText input = new EditText(this);
        input.setHint("e.g. 10.76.84.167");
        input.setText("10.76.84.167");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String ip = input.getText().toString().trim();
            if (!ip.isEmpty()) {
                ApiClient.setServerIp(ip);
                if (txtCurrentServerIp != null) {
                    txtCurrentServerIp.setText(ApiClient.BASE_URL);
                }
                Toast.makeText(this, "Server URL updated to: " + ApiClient.BASE_URL, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void testBackendConnection() {
        Toast.makeText(this, "Testing connection to " + ApiClient.BASE_URL + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getDashboardApi(this).getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "✅ Connection Successful! (HTTP 200 OK)", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "⚠️ Server responded with HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "❌ Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}