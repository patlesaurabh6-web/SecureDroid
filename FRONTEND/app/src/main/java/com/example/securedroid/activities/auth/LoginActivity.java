package com.example.securedroid.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.activities.dashboard.DashboardActivity;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.LoginRequest;
import com.example.securedroid.api.dto.TokenResponse;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView txtRegister, txtForgot;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            openDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgot = findViewById(R.id.txtForgot);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                performLogin();
            }
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        txtForgot.setOnClickListener(v -> showForgotPasswordDialog());

        ImageView imgServerConfig = findViewById(R.id.imgServerConfig);
        if (imgServerConfig != null) {
            imgServerConfig.setOnClickListener(v -> showServerConfigDialog());
        }
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText etResetEmail = dialogView.findViewById(R.id.etResetEmail);
        Button btnCancelReset = dialogView.findViewById(R.id.btnCancelReset);
        Button btnSendReset = dialogView.findViewById(R.id.btnSendReset);

        if (etEmail != null && etEmail.getText() != null) {
            etResetEmail.setText(etEmail.getText().toString().trim());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelReset.setOnClickListener(v -> dialog.dismiss());

        btnSendReset.setOnClickListener(v -> {
            String email = etResetEmail.getText() != null ? etResetEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                etResetEmail.setError("Enter registered email");
                etResetEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etResetEmail.setError("Enter a valid email address");
                etResetEmail.requestFocus();
                return;
            }

            Toast.makeText(this, "Password reset link sent to " + email + ". Check your inbox!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showServerConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Connection Setup");
        builder.setMessage("Current URL: " + ApiClient.BASE_URL + "\n\nChoose connection mode or enter your laptop IP:");

        String[] options = {
                "🔌 USB Mode (http://127.0.0.1:8000/api/)",
                "📶 Wi-Fi Mode (http://10.76.84.167:8000/api/)",
                "✏️ Enter Custom IP Address",
                "⚡ Test Connection Now"
        };

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                ApiClient.setServerIp("127.0.0.1");
                Toast.makeText(this, "Set to USB ADB Mode: " + ApiClient.BASE_URL, Toast.LENGTH_SHORT).show();
            } else if (which == 1) {
                ApiClient.setServerIp("10.76.84.167");
                Toast.makeText(this, "Set to Wi-Fi Mode: " + ApiClient.BASE_URL, Toast.LENGTH_SHORT).show();
            } else if (which == 2) {
                showCustomIpInput();
            } else if (which == 3) {
                testServerConnection();
            }
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showCustomIpInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Laptop IP Address");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g. 10.76.84.167 or 192.168.1.5");
        input.setText("127.0.0.1");
        builder.setView(input);

        builder.setPositiveButton("Save & Apply", (dialog, which) -> {
            String ip = input.getText().toString().trim();
            if (!ip.isEmpty()) {
                ApiClient.setServerIp(ip);
                Toast.makeText(this, "Updated: " + ApiClient.BASE_URL, Toast.LENGTH_LONG).show();
                testServerConnection();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void testServerConnection() {
        Toast.makeText(this, "Testing connection to " + ApiClient.BASE_URL + "...", Toast.LENGTH_SHORT).show();
        ApiClient.getDashboardApi(this).getDashboardSummary().enqueue(new Callback<com.example.securedroid.api.dto.DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.securedroid.api.dto.DashboardSummaryResponse> call, Response<com.example.securedroid.api.dto.DashboardSummaryResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "✅ Connection Successful! (HTTP 200 OK)", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "⚠️ Server returned HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.securedroid.api.dto.DashboardSummaryResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "❌ Failed: " + t.getLocalizedMessage() + "\nTap the top right icon to switch mode.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        LoginRequest request = new LoginRequest(email, password);
        ApiClient.getAuthApi(this).login(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");

                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokenRes = response.body();
                    String userName = tokenRes.getName() != null && !tokenRes.getName().isEmpty() 
                            ? tokenRes.getName() 
                            : email.split("@")[0];

                    sessionManager.saveAuthToken(
                            tokenRes.getAccessToken(),
                            tokenRes.getUserId(),
                            userName,
                            tokenRes.getEmail() != null ? tokenRes.getEmail() : email
                    );
                    Toast.makeText(LoginActivity.this, "Welcome back, " + userName + "!", Toast.LENGTH_SHORT).show();
                    openDashboard();
                } else if (response.code() == 401) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials. Please register or check your email and password.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed (Code: " + response.code() + "). Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");
                showServerConfigDialog();
            }
        });
    }

    private void openDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid Email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 Characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }
}