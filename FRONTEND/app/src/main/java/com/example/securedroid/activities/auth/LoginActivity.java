package com.example.securedroid.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
                Toast.makeText(LoginActivity.this, "Cannot connect to backend server: " + t.getLocalizedMessage() + "\nPlease verify your backend is running.", Toast.LENGTH_LONG).show();
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