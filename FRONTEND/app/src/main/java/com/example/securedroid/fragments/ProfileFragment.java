package com.example.securedroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;
import com.example.securedroid.activities.NotificationHistoryActivity;
import com.example.securedroid.activities.PermissionManagerActivity;
import com.example.securedroid.activities.PrivacyReportActivity;
import com.example.securedroid.activities.SettingsActivity;
import com.example.securedroid.activities.auth.LoginActivity;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.models.UserModel;
import com.example.securedroid.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView txtProfileName, txtProfileEmail;
    private RelativeLayout optionSettings, optionPrivacyReport, optionPermissionManager, optionNotificationHistory;
    private Button btnLogout;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);

        optionSettings = view.findViewById(R.id.optionSettings);
        optionPrivacyReport = view.findViewById(R.id.optionPrivacyReport);
        optionPermissionManager = view.findViewById(R.id.optionPermissionManager);
        optionNotificationHistory = view.findViewById(R.id.optionNotificationHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        populateUserProfile();
        setupClickListeners();

        return view;
    }

    private void populateUserProfile() {
        if (sessionManager != null) {
            String name = sessionManager.getUserName();
            String email = sessionManager.getUserEmail();

            if (txtProfileName != null && name != null && !name.isEmpty()) {
                txtProfileName.setText(name);
            }
            if (txtProfileEmail != null && email != null && !email.isEmpty()) {
                txtProfileEmail.setText(email);
            }
        }

        // Fetch fresh profile from backend API if connected
        if (getContext() != null) {
            ApiClient.getAuthApi(getContext()).getMe().enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserModel user = response.body();
                        if (txtProfileName != null && user.getName() != null) {
                            txtProfileName.setText(user.getName());
                        }
                        if (txtProfileEmail != null && user.getEmail() != null) {
                            txtProfileEmail.setText(user.getEmail());
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    // Retain session data
                }
            });
        }
    }

    private void setupClickListeners() {
        optionSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        optionPrivacyReport.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PrivacyReportActivity.class);
            startActivity(intent);
        });

        optionPermissionManager.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PermissionManagerActivity.class);
            startActivity(intent);
        });

        optionNotificationHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        if (getActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout from this account?");
        builder.setPositiveButton("LOGOUT", (dialog, which) -> {
            SessionManager.getInstance(getActivity()).logout();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}