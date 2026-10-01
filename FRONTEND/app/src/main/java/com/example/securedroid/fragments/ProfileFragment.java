package com.example.securedroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView txtProfileName, txtProfileEmail;
    private TextView txtProfileAlertsCount, txtProfileAppsCount, txtProfileStatus;
    private RelativeLayout optionSettings, optionPrivacyReport, optionPermissionManager, optionNotificationHistory;
    private Button btnLogout;
    private SessionManager sessionManager;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && getContext() != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception ignored) {}

                            sessionManager.saveUserProfile(null, null, selectedImageUri.toString());
                            if (imgAvatar != null) {
                                imgAvatar.setImageURI(selectedImageUri);
                                imgAvatar.setPadding(0, 0, 0, 0);
                            }
                            Toast.makeText(getContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);

        txtProfileAlertsCount = view.findViewById(R.id.txtProfileAlertsCount);
        txtProfileAppsCount = view.findViewById(R.id.txtProfileAppsCount);
        txtProfileStatus = view.findViewById(R.id.txtProfileStatus);

        optionSettings = view.findViewById(R.id.optionSettings);
        optionPrivacyReport = view.findViewById(R.id.optionPrivacyReport);
        optionPermissionManager = view.findViewById(R.id.optionPermissionManager);
        optionNotificationHistory = view.findViewById(R.id.optionNotificationHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        populateUserProfile();
        calculateActualMetrics();
        setupClickListeners();

        return view;
    }

    private void populateUserProfile() {
        if (sessionManager != null) {
            String name = sessionManager.getUserName();
            String email = sessionManager.getUserEmail();
            String avatarUri = sessionManager.getAvatarUri();

            if (txtProfileName != null && name != null && !name.isEmpty()) {
                txtProfileName.setText(name);
            }
            if (txtProfileEmail != null && email != null && !email.isEmpty()) {
                txtProfileEmail.setText(email);
            }
            if (imgAvatar != null && avatarUri != null && !avatarUri.isEmpty()) {
                try {
                    imgAvatar.setImageURI(Uri.parse(avatarUri));
                    imgAvatar.setPadding(0, 0, 0, 0);
                } catch (Exception ignored) {}
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
                            sessionManager.saveUserProfile(user.getName(), null, null);
                        }
                        if (txtProfileEmail != null && user.getEmail() != null) {
                            txtProfileEmail.setText(user.getEmail());
                            sessionManager.saveUserProfile(null, user.getEmail(), null);
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

    private void calculateActualMetrics() {
        if (getContext() == null) return;

        // Metric 1: Real Alerts count
        int realAlerts = sessionManager != null ? sessionManager.getPrivacyAlerts().size() : 0;
        if (txtProfileAlertsCount != null) {
            txtProfileAlertsCount.setText(String.valueOf(realAlerts));
        }

        // Metric 2: Real Apps Scanned
        int realApps = PackageManagerHelper.getInstalledApps(requireContext()).size();
        if (txtProfileAppsCount != null) {
            txtProfileAppsCount.setText(String.valueOf(realApps));
        }

        // Metric 3: Real Protection status
        if (txtProfileStatus != null) {
            txtProfileStatus.setText("Active");
        }
    }

    private void setupClickListeners() {
        // Change avatar on click
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> showAvatarOptionsDialog());
        }

        // Edit Profile Info on click
        View.OnClickListener editProfileListener = v -> showEditProfileDialog();
        if (txtProfileName != null) txtProfileName.setOnClickListener(editProfileListener);
        if (txtProfileEmail != null) txtProfileEmail.setOnClickListener(editProfileListener);

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

    private void showAvatarOptionsDialog() {
        if (getContext() == null) return;
        String[] options = {"Choose from Gallery", "Reset to Default Icon"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        imagePickerLauncher.launch(intent);
                    } else {
                        sessionManager.saveUserProfile(null, null, "");
                        if (imgAvatar != null) {
                            imgAvatar.setImageResource(R.drawable.ic_person);
                            imgAvatar.setPadding((int)(16 * getResources().getDisplayMetrics().density),
                                    (int)(16 * getResources().getDisplayMetrics().density),
                                    (int)(16 * getResources().getDisplayMetrics().density),
                                    (int)(16 * getResources().getDisplayMetrics().density));
                        }
                        Toast.makeText(getContext(), "Avatar reset to default", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showEditProfileDialog() {
        if (getContext() == null) return;

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        final EditText etName = new EditText(requireContext());
        etName.setHint("Full Name");
        etName.setText(sessionManager.getUserName());
        layout.addView(etName);

        final EditText etEmail = new EditText(requireContext());
        etEmail.setHint("Email Address");
        etEmail.setText(sessionManager.getUserEmail());
        layout.addView(etEmail);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();

                    if (!newName.isEmpty()) {
                        sessionManager.saveUserProfile(newName, newEmail, null);
                        if (txtProfileName != null) txtProfileName.setText(newName);
                        if (txtProfileEmail != null) txtProfileEmail.setText(newEmail);
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
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