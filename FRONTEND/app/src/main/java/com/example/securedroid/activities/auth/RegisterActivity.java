package com.example.securedroid.activities.auth;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.RegisterRequest;
import com.example.securedroid.models.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                performRegister();
            }
        });

        txtLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        RegisterRequest req = new RegisterRequest(name, email, mobile, password);
        ApiClient.getAuthApi(this).register(req).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("REGISTER");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Account created successfully for " + name + "! Please sign in.", Toast.LENGTH_LONG).show();
                    finish();
                } else if (response.code() == 409) {
                    Toast.makeText(RegisterActivity.this, "Email address is already registered.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("REGISTER");
                Toast.makeText(RegisterActivity.this, "Network connection error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Enter Full Name");
            etName.requestFocus();
            return false;
        }
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            return false;
        }
        if (etMobile.getText().toString().trim().isEmpty()) {
            etMobile.setError("Enter Mobile Number");
            etMobile.requestFocus();
            return false;
        }
        if (etPassword.getText().toString().trim().isEmpty()) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            return false;
        }
        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }
}