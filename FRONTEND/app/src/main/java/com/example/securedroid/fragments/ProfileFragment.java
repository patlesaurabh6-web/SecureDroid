package com.example.securedroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
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
import com.example.securedroid.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private RelativeLayout optionSettings, optionPrivacyReport, optionPermissionManager, optionNotificationHistory;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        optionSettings = view.findViewById(R.id.optionSettings);
        optionPrivacyReport = view.findViewById(R.id.optionPrivacyReport);
        optionPermissionManager = view.findViewById(R.id.optionPermissionManager);
        optionNotificationHistory = view.findViewById(R.id.optionNotificationHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupClickListeners();

        return view;
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
        builder.setMessage("Are you sure you want to logout from the app?");
        builder.setPositiveButton("LOGOUT", (dialog, which) -> {
            // Clear session tokens and login state
            SessionManager.getInstance(getActivity()).logout();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to Login Screen clearing activity stack
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}