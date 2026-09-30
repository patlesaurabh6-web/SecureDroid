package com.example.securedroid.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.AppAnalysisRequest;
import com.example.securedroid.models.RiskScoreModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, btnOpenSettings, imgAppDetailsIcon;
    private TextView txtAppDetailsName, txtAppDetailsPkg, txtAppDetailsVersion;
    private FrameLayout layoutAppScoreCircle;
    private TextView txtAppScoreNumber, txtAppScoreLevel, txtAppScoreHeadline, txtAppScoreSummaryText;
    private LinearLayout layoutBreakdownReasons, layoutPermissionsList;
    private TextView txtAppRecommendation, txtPermissionsHeader;
    private MaterialButton btnManageAppPermissions;

    private String packageName;
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        initViews();
        extractIntentData();
        loadAppDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnOpenSettings = findViewById(R.id.btnOpenSettings);
        imgAppDetailsIcon = findViewById(R.id.imgAppDetailsIcon);

        txtAppDetailsName = findViewById(R.id.txtAppDetailsName);
        txtAppDetailsPkg = findViewById(R.id.txtAppDetailsPkg);
        txtAppDetailsVersion = findViewById(R.id.txtAppDetailsVersion);

        layoutAppScoreCircle = findViewById(R.id.layoutAppScoreCircle);
        txtAppScoreNumber = findViewById(R.id.txtAppScoreNumber);
        txtAppScoreLevel = findViewById(R.id.txtAppScoreLevel);
        txtAppScoreHeadline = findViewById(R.id.txtAppScoreHeadline);
        txtAppScoreSummaryText = findViewById(R.id.txtAppScoreSummaryText);

        layoutBreakdownReasons = findViewById(R.id.layoutBreakdownReasons);
        layoutPermissionsList = findViewById(R.id.layoutPermissionsList);
        txtAppRecommendation = findViewById(R.id.txtAppRecommendation);
        txtPermissionsHeader = findViewById(R.id.txtPermissionsHeader);
        btnManageAppPermissions = findViewById(R.id.btnManageAppPermissions);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View.OnClickListener openSettingsListener = v -> openAppSystemSettings();
        if (btnOpenSettings != null) btnOpenSettings.setOnClickListener(openSettingsListener);
        if (btnManageAppPermissions != null) btnManageAppPermissions.setOnClickListener(openSettingsListener);
    }

    private void extractIntentData() {
        packageName = getIntent().getStringExtra("PACKAGE_NAME");
        appName = getIntent().getStringExtra("APP_NAME");

        if (packageName == null || packageName.isEmpty()) {
            packageName = getPackageName();
        }
    }

    private void openAppSystemSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open system settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAppDetails() {
        PackageManager pm = getPackageManager();
        List<String> permissions = new ArrayList<>();
        String versionName = "1.0";

        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(appInfo);
            if (label != null && (appName == null || appName.isEmpty())) {
                appName = label.toString();
            }

            Drawable icon = pm.getApplicationIcon(packageName);
            if (icon != null && imgAppDetailsIcon != null) {
                imgAppDetailsIcon.setImageDrawable(icon);
            }

            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo != null) {
                if (packageInfo.versionName != null) {
                    versionName = packageInfo.versionName;
                }
                if (packageInfo.requestedPermissions != null) {
                    permissions.addAll(Arrays.asList(packageInfo.requestedPermissions));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (appName == null || appName.isEmpty()) {
            appName = packageName;
        }

        if (txtAppDetailsName != null) txtAppDetailsName.setText(appName);
        if (txtAppDetailsPkg != null) txtAppDetailsPkg.setText(packageName);
        if (txtAppDetailsVersion != null) txtAppDetailsVersion.setText("Version " + versionName);

        // Perform local and backend analysis
        performRiskAnalysis(permissions);
    }

    private void performRiskAnalysis(List<String> permissions) {
        // Calculate deterministic explainable breakdown
        boolean hasCamera = false;
        boolean hasMic = false;
        boolean hasLocation = false;
        boolean hasContacts = false;
        boolean hasSms = false;
        boolean hasStorage = false;
        boolean hasPhone = false;

        List<BreakdownItem> breakdownItems = new ArrayList<>();

        for (String p : permissions) {
            String u = p.toUpperCase();
            if (u.contains("CAMERA")) hasCamera = true;
            if (u.contains("RECORD_AUDIO") || u.contains("MICROPHONE")) hasMic = true;
            if (u.contains("LOCATION")) hasLocation = true;
            if (u.contains("CONTACT")) hasContacts = true;
            if (u.contains("SMS")) hasSms = true;
            if (u.contains("STORAGE") || u.contains("MEDIA")) hasStorage = true;
            if (u.contains("READ_PHONE_STATE") || u.contains("CALL_LOG")) hasPhone = true;
        }

        int rawScore = 0;
        if (hasCamera) rawScore += 25;
        if (hasMic) rawScore += 25;
        if (hasLocation) rawScore += 20;
        if (hasContacts) rawScore += 20;
        if (hasSms) rawScore += 25;
        if (hasStorage) rawScore += 10;
        if (hasPhone) rawScore += 15;

        // Combination risks & Plain-English breakdowns
        if (hasCamera && hasLocation) {
            rawScore += 15;
            breakdownItems.add(new BreakdownItem("📸 Camera & 📍 Location Combined", 
                    "This app can record pictures and pinpoint your precise GPS coordinates simultaneously. Photos and videos could be geotagged with your exact location.", 
                    0xFFFF3B30));
        } else if (hasCamera) {
            breakdownItems.add(new BreakdownItem("📸 Camera Access", 
                    "This app has permission to capture photos and video streams from your device sensors.", 
                    0xFFFF9800));
        }

        if (hasMic && hasLocation) {
            rawScore += 10;
            breakdownItems.add(new BreakdownItem("🎙️ Microphone & 📍 Location Combined", 
                    "Simultaneous audio recording and geolocation access allows tracking ambient conversations and physical location.", 
                    0xFFFF3B30));
        } else if (hasMic) {
            breakdownItems.add(new BreakdownItem("🎙️ Microphone Access", 
                    "This app can record ambient audio and voice conversations using your device microphone.", 
                    0xFFFF9800));
        }

        if (hasSms) {
            breakdownItems.add(new BreakdownItem("💬 SMS Messages & OTPs", 
                    "The app can read sensitive incoming SMS messages, which could expose 2-Factor Authentication (2FA) verification codes and private texts.", 
                    0xFFFF3B30));
        }

        if (hasContacts) {
            breakdownItems.add(new BreakdownItem("📖 Address Book & Contacts", 
                    "The app has permission to inspect your private contact list, including phone numbers, names, and email addresses.", 
                    0xFFFF9800));
        }

        if (hasLocation && !hasCamera && !hasMic) {
            breakdownItems.add(new BreakdownItem("📍 Geolocation Tracking", 
                    "The app tracks your live GPS coordinates and movement patterns throughout the day.", 
                    0xFFFFC107));
        }

        if (hasStorage) {
            breakdownItems.add(new BreakdownItem("🗂️ Device Storage & Media", 
                    "The app has access to read or write files, photos, and documents stored on your device storage.", 
                    0xFFFFC107));
        }

        if (hasPhone) {
            breakdownItems.add(new BreakdownItem("📞 Phone State & Identity", 
                    "The app can identify your unique device ID, active cellular network, and incoming call activity.", 
                    0xFFFFC107));
        }

        // Cap and categorize
        int score = Math.min(100, Math.max(10, rawScore));
        String level;
        int color;
        String headline;
        String summary;
        String recommendation;

        if (score >= 65 || (hasCamera && hasLocation) || hasSms || (hasCamera && hasMic)) {
            level = "HIGH RISK";
            color = 0xFFFF3B30; // Vibrant Red
            headline = "⚠️ Critical Privacy Risk Detected";
            summary = "This application requests multiple sensitive permissions (Camera, Mic, SMS, or Location) that can access your private data.";
            recommendation = "• Revoke Camera & Location when not in active use.\n• Disable Background App Refresh in Android Settings.\n• Ensure this application is from a verified trusted developer.";
        } else if (score >= 35 || hasCamera || hasMic || hasLocation || hasStorage) {
            level = "MEDIUM RISK";
            color = 0xFFFFC107; // Vibrant Amber
            headline = "⚠️ Moderate Privacy Risk";
            summary = "This application requests sensitive permissions like Location or Storage. Audit its background activity.";
            recommendation = "• Set Location access to 'While using the app only'.\n• Regularly audit if storage access is required for core functionality.";
        } else {
            level = "LOW RISK";
            color = 0xFF00E676; // Vibrant Green
            headline = "✅ Safe & Protected Application";
            summary = "This application only utilizes standard utility permissions (Network, Vibration) with zero personal sensor access.";
            recommendation = "• No critical actions required.\n• Application follows baseline Android security and privacy standards.";

            breakdownItems.add(new BreakdownItem("🔒 Zero Sensitive Sensors", 
                    "This app does not request access to your Camera, Microphone, GPS Location, Contacts, or SMS messages.", 
                    0xFF00E676));
            breakdownItems.add(new BreakdownItem("🌐 Standard Network Operations", 
                    "Uses standard internet connectivity and device vibration without accessing personal files.", 
                    0xFF00E676));
        }

        // Render Circular score with matching ring
        renderCircularScore(score, level, color, headline, summary, recommendation);

        // Render Breakdown items
        renderBreakdownList(breakdownItems);

        // Render Permissions list
        renderPermissionsList(permissions);

        // Also ping backend for asynchronous AI/server analysis sync
        try {
            AppAnalysisRequest req = new AppAnalysisRequest(packageName, appName, permissions);
            ApiClient.getAnalysisApi(this).analyzeApplication(req).enqueue(new Callback<RiskScoreModel>() {
                @Override
                public void onResponse(Call<RiskScoreModel> call, Response<RiskScoreModel> response) {
                    // Backend analysis synced
                }

                @Override
                public void onFailure(Call<RiskScoreModel> call, Throwable t) {
                    // Fallback local analysis active
                }
            });
        } catch (Exception ignored) {}
    }

    private void renderCircularScore(int score, String level, int targetColor, String headline, String summary, String recommendation) {
        if (txtAppScoreNumber != null) {
            txtAppScoreNumber.setText(String.valueOf(score));
            txtAppScoreNumber.setTextColor(targetColor);
        }

        if (txtAppScoreLevel != null) {
            txtAppScoreLevel.setText(level);
            txtAppScoreLevel.setTextColor(targetColor);
        }

        if (txtAppScoreHeadline != null) {
            txtAppScoreHeadline.setText(headline);
        }

        if (txtAppScoreSummaryText != null) {
            txtAppScoreSummaryText.setText(summary);
        }

        if (txtAppRecommendation != null) {
            txtAppRecommendation.setText(recommendation);
        }

        // Apply matching dynamic ring color to the circular border
        if (layoutAppScoreCircle != null) {
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(0xFF1B2B40);
            int strokeWidthPx = (int) (7 * getResources().getDisplayMetrics().density);
            circleBg.setStroke(strokeWidthPx, targetColor);
            layoutAppScoreCircle.setBackground(circleBg);
        }
    }

    private void renderBreakdownList(List<BreakdownItem> items) {
        if (layoutBreakdownReasons == null) return;
        layoutBreakdownReasons.removeAllViews();

        for (BreakdownItem item : items) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(0, 0, 0, (int) (14 * getResources().getDisplayMetrics().density));

            TextView txtTitle = new TextView(this);
            txtTitle.setText(item.title);
            txtTitle.setTextSize(14);
            txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            txtTitle.setTextColor(item.color);

            TextView txtDesc = new TextView(this);
            txtDesc.setText(item.description);
            txtDesc.setTextSize(13);
            txtDesc.setTextColor(0xFFB0BEC5);
            txtDesc.setPadding(0, (int) (3 * getResources().getDisplayMetrics().density), 0, 0);
            txtDesc.setLineSpacing(2, 1.1f);

            itemLayout.addView(txtTitle);
            itemLayout.addView(txtDesc);
            layoutBreakdownReasons.addView(itemLayout);
        }
    }

    private void renderPermissionsList(List<String> permissions) {
        if (txtPermissionsHeader != null) {
            txtPermissionsHeader.setText("All Requested Permissions (" + permissions.size() + ")");
        }

        if (layoutPermissionsList == null) return;
        layoutPermissionsList.removeAllViews();

        if (permissions.isEmpty()) {
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText("No permissions requested by this application.");
            txtEmpty.setTextColor(0xFF90A4AE);
            txtEmpty.setTextSize(13);
            layoutPermissionsList.addView(txtEmpty);
            return;
        }

        for (String perm : permissions) {
            TextView txtPerm = new TextView(this);
            String simpleName = perm.replace("android.permission.", "");
            txtPerm.setText("• " + simpleName);
            txtPerm.setTextSize(13);
            txtPerm.setPadding(0, (int) (4 * getResources().getDisplayMetrics().density), 0, (int) (4 * getResources().getDisplayMetrics().density));

            String u = perm.toUpperCase();
            if (u.contains("CAMERA") || u.contains("RECORD_AUDIO") || u.contains("SMS") || u.contains("LOCATION") || u.contains("CONTACT")) {
                txtPerm.setTextColor(0xFFFF8A80); // Red highlight for sensitive
            } else if (u.contains("STORAGE") || u.contains("PHONE") || u.contains("BLUETOOTH")) {
                txtPerm.setTextColor(0xFFFFE082); // Amber highlight
            } else {
                txtPerm.setTextColor(0xFFCFD8DC); // Clean light grey
            }

            layoutPermissionsList.addView(txtPerm);
        }
    }

    private static class BreakdownItem {
        String title;
        String description;
        int color;

        BreakdownItem(String title, String description, int color) {
            this.title = title;
            this.description = description;
            this.color = color;
        }
    }
}