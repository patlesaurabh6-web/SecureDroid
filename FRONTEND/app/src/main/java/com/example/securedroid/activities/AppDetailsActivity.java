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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.AppAnalysisRequest;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.models.RecommendationModel;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        PackageManagerHelper.AppPermissionBreakdown breakdown = new PackageManagerHelper.AppPermissionBreakdown();
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
                breakdown = PackageManagerHelper.classifyPermissions(pm, packageInfo, packageName);
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

        // Perform analysis using authoritative backend RiskScoreModel using accurately classified permissions
        performRiskAnalysis(breakdown, versionName);
    }

    private void performRiskAnalysis(PackageManagerHelper.AppPermissionBreakdown b, String versionName) {
        // Render permissions list with explicit Allowed / Not Allowed / Normal status
        renderPermissionsList(b.grantedSensitivePermissions, b.deniedSensitivePermissions, b.normalPermissions);

        com.example.securedroid.utils.SessionManager sessionManager = com.example.securedroid.utils.SessionManager.getInstance(this);

        // If no sensitive permissions are currently granted to this app, risk is baseline safe
        if (b.grantedSensitivePermissions.isEmpty()) {
            RiskScoreModel zeroRiskModel = new RiskScoreModel(10.0f, "LOW", "No sensitive permissions are currently granted to this application in Android Settings.");
            applyRiskScoreModel(zeroRiskModel, b.grantedSensitivePermissions);
            sessionManager.saveAppRiskScore(packageName, zeroRiskModel);
            return;
        }

        // Check if authoritative RiskScoreModel was already computed & cached by backend
        RiskScoreModel cachedModel = sessionManager.getAppRiskScore(packageName);
        if (cachedModel != null) {
            applyRiskScoreModel(cachedModel, b.grantedSensitivePermissions);
        } else {
            showLoadingState();
        }

        // Query backend risk engine evaluating ONLY the currently granted permissions
        try {
            AppAnalysisRequest req = new AppAnalysisRequest(
                    packageName, appName, versionName, "Installed App",
                    b.declaredPermissions, b.grantedSensitivePermissions,
                    b.deniedSensitivePermissions, b.normalPermissions
            );
            ApiClient.getAnalysisApi(this).analyzeApplication(req).enqueue(new Callback<RiskScoreModel>() {
                @Override
                public void onResponse(Call<RiskScoreModel> call, Response<RiskScoreModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RiskScoreModel model = response.body();
                        sessionManager.saveAppRiskScore(packageName, model);
                        applyRiskScoreModel(model, b.grantedSensitivePermissions);
                    } else if (cachedModel == null) {
                        showBackendUnavailableState();
                    }
                }

                @Override
                public void onFailure(Call<RiskScoreModel> call, Throwable t) {
                    if (cachedModel == null) {
                        showBackendUnavailableState();
                    }
                }
            });
        } catch (Exception e) {
            if (cachedModel == null) {
                showBackendUnavailableState();
            }
        }
    }

    private void showLoadingState() {
        if (txtAppScoreNumber != null) {
            txtAppScoreNumber.setText("--");
            txtAppScoreNumber.setTextColor(0xFF90A4AE);
        }
        if (txtAppScoreLevel != null) {
            txtAppScoreLevel.setText("ANALYZING");
            txtAppScoreLevel.setTextColor(0xFF90A4AE);
        }
        if (txtAppScoreHeadline != null) {
            txtAppScoreHeadline.setText("Evaluating Application Privacy...");
        }
        if (txtAppScoreSummaryText != null) {
            txtAppScoreSummaryText.setText("Submitting permission declarations to SecureDroid backend risk engine...");
        }
        if (txtAppRecommendation != null) {
            txtAppRecommendation.setText("• Running authoritative risk evaluation via calculate_privacy_risk().");
        }
        if (layoutAppScoreCircle != null) {
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(0xFF1B2B40);
            int strokeWidthPx = (int) (7 * getResources().getDisplayMetrics().density);
            circleBg.setStroke(strokeWidthPx, 0xFF90A4AE);
            layoutAppScoreCircle.setBackground(circleBg);
        }
    }

    private void showBackendUnavailableState() {
        if (txtAppScoreNumber != null) {
            txtAppScoreNumber.setText("--");
            txtAppScoreNumber.setTextColor(0xFF78909C);
        }
        if (txtAppScoreLevel != null) {
            txtAppScoreLevel.setText("SERVER OFFLINE");
            txtAppScoreLevel.setTextColor(0xFF78909C);
        }
        if (txtAppScoreHeadline != null) {
            txtAppScoreHeadline.setText("Backend Risk Engine Unavailable");
        }
        if (txtAppScoreSummaryText != null) {
            txtAppScoreSummaryText.setText("Could not reach backend risk engine at http://127.0.0.1:8000. Start the backend server to calculate authoritative privacy risk.");
        }
        if (txtAppRecommendation != null) {
            txtAppRecommendation.setText("• Start backend: uvicorn app.main:app --port 8000\n• If testing on physical phone: adb reverse tcp:8000 tcp:8000");
        }
        if (layoutAppScoreCircle != null) {
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(0xFF1B2B40);
            int strokeWidthPx = (int) (7 * getResources().getDisplayMetrics().density);
            circleBg.setStroke(strokeWidthPx, 0xFF78909C);
            layoutAppScoreCircle.setBackground(circleBg);
        }
    }

    private void applyRiskScoreModel(RiskScoreModel model, List<String> permissions) {
        if (model == null) return;

        int score = Math.round(model.getRiskScore());
        String level = model.getRiskLevel() != null ? model.getRiskLevel().toUpperCase() : "LOW";
        String summary = model.getAnalysisSummary() != null ? model.getAnalysisSummary() : "";

        int color;
        String headline;
        if ("CRITICAL".equalsIgnoreCase(level) || "HIGH".equalsIgnoreCase(level) || score >= 65) {
            level = "HIGH RISK";
            color = 0xFFFF3B30; // Vibrant Red
            headline = "⚠️ Critical Privacy Risk Detected";
        } else if ("MEDIUM".equalsIgnoreCase(level) || score >= 35) {
            level = "MEDIUM RISK";
            color = 0xFFFFC107; // Vibrant Amber
            headline = "⚠️ Moderate Privacy Risk";
        } else {
            level = "LOW RISK";
            color = 0xFF00E676; // Vibrant Green
            headline = "✅ Safe & Protected Application";
        }

        // Recommendations from backend RiskScoreModel
        StringBuilder recBuilder = new StringBuilder();
        if (model.getRecommendations() != null && !model.getRecommendations().isEmpty()) {
            for (RecommendationModel rec : model.getRecommendations()) {
                if (rec.getTitle() != null && !rec.getTitle().isEmpty()) {
                    recBuilder.append("• ").append(rec.getTitle());
                    if (rec.getRecommendation() != null && !rec.getRecommendation().isEmpty()) {
                        recBuilder.append(": ").append(rec.getRecommendation());
                    }
                    recBuilder.append("\n");
                }
            }
        }
        String recommendation = recBuilder.length() > 0 ? recBuilder.toString().trim() : ("• Privacy evaluation provided by SecureDroid backend risk engine.");

        // Reasons & Breakdown from backend RiskScoreModel
        List<BreakdownItem> breakdownItems = new ArrayList<>();
        if (model.getReasons() != null && !model.getReasons().isEmpty()) {
            for (String reason : model.getReasons()) {
                breakdownItems.add(new BreakdownItem("Privacy Risk Factor", reason, color));
            }
        } else {
            breakdownItems.add(new BreakdownItem("Authoritative Evaluation", summary.isEmpty() ? "No sensitive risk factors detected." : summary, color));
        }

        renderCircularScore(score, level, color, headline, summary, recommendation);
        renderBreakdownList(breakdownItems);
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

    private void renderPermissionsList(List<String> grantedPermissions, List<String> notGrantedPermissions, List<String> normalPermissions) {
        int grantedCount = grantedPermissions != null ? grantedPermissions.size() : 0;
        int notGrantedCount = notGrantedPermissions != null ? notGrantedPermissions.size() : 0;
        int normalCount = normalPermissions != null ? normalPermissions.size() : 0;
        int totalDeclared = grantedCount + notGrantedCount + normalCount;

        if (txtPermissionsHeader != null) {
            txtPermissionsHeader.setText("App Permissions (" + totalDeclared + " Declared)");
        }

        if (layoutPermissionsList == null) return;
        layoutPermissionsList.removeAllViews();

        float density = getResources().getDisplayMetrics().density;

        // --- Section 1: Allowed / Granted Sensitive Permissions ---
        TextView txtGrantedTitle = new TextView(this);
        txtGrantedTitle.setText("ALLOWED SENSITIVE PERMISSIONS (" + grantedCount + ")");
        txtGrantedTitle.setTextColor(0xFF00E676);
        txtGrantedTitle.setTextSize(13);
        txtGrantedTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        txtGrantedTitle.setPadding(0, (int) (6 * density), 0, (int) (6 * density));
        layoutPermissionsList.addView(txtGrantedTitle);

        if (grantedCount == 0) {
            TextView txtEmptyGranted = new TextView(this);
            txtEmptyGranted.setText("No sensitive permissions are currently allowed in Android Settings.");
            txtEmptyGranted.setTextColor(0xFF90A4AE);
            txtEmptyGranted.setTextSize(12);
            txtEmptyGranted.setPadding(0, 0, 0, (int) (10 * density));
            layoutPermissionsList.addView(txtEmptyGranted);
        } else {
            for (String perm : grantedPermissions) {
                layoutPermissionsList.addView(createPermissionRow(perm, "ALLOWED", 0xFF00E676, 0x2800E676, density));
            }
        }

        // --- Section 2: Not Granted / Denied Sensitive Permissions ---
        if (notGrantedCount > 0) {
            TextView txtDeniedTitle = new TextView(this);
            txtDeniedTitle.setText("NOT ALLOWED / DENIED PERMISSIONS (" + notGrantedCount + ")");
            txtDeniedTitle.setTextColor(0xFF78909C);
            txtDeniedTitle.setTextSize(13);
            txtDeniedTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            txtDeniedTitle.setPadding(0, (int) (14 * density), 0, (int) (6 * density));
            layoutPermissionsList.addView(txtDeniedTitle);

            for (String perm : notGrantedPermissions) {
                layoutPermissionsList.addView(createPermissionRow(perm, "NOT ALLOWED", 0xFF78909C, 0x1F78909C, density));
            }
        }

        // --- Section 3: Normal / Install-Time Capabilities ---
        if (normalCount > 0) {
            TextView txtNormalTitle = new TextView(this);
            txtNormalTitle.setText("NORMAL / INSTALL-TIME CAPABILITIES (" + normalCount + ")");
            txtNormalTitle.setTextColor(0xFF81D4FA);
            txtNormalTitle.setTextSize(13);
            txtNormalTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            txtNormalTitle.setPadding(0, (int) (14 * density), 0, (int) (6 * density));
            layoutPermissionsList.addView(txtNormalTitle);

            for (String perm : normalPermissions) {
                layoutPermissionsList.addView(createPermissionRow(perm, "NORMAL", 0xFF81D4FA, 0x1F81D4FA, density));
            }
        }
    }

    private View createPermissionRow(String perm, String badgeText, int badgeColor, int badgeBgColor, float density) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, (int) (5 * density), 0, (int) (5 * density));

        TextView txtName = new TextView(this);
        String simpleName = perm.replace("android.permission.", "");
        boolean isAllowed = "ALLOWED".equalsIgnoreCase(badgeText);
        txtName.setText((isAllowed ? "• " : "○ ") + simpleName);
        txtName.setTextSize(13);
        txtName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        if (isAllowed) {
            String u = perm.toUpperCase(java.util.Locale.ROOT);
            if (u.contains("CAMERA") || u.contains("RECORD_AUDIO") || u.contains("SMS") || u.contains("LOCATION") || u.contains("CONTACT")) {
                txtName.setTextColor(0xFFFF8A80);
            } else if (u.contains("STORAGE") || u.contains("PHONE") || u.contains("BLUETOOTH")) {
                txtName.setTextColor(0xFFFFE082);
            } else {
                txtName.setTextColor(0xFFE0E0E0);
            }
        } else {
            txtName.setTextColor(0xFF90A4AE);
        }
        row.addView(txtName);

        TextView txtBadge = new TextView(this);
        txtBadge.setText(badgeText);
        txtBadge.setTextSize(10);
        txtBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        txtBadge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(6 * density);
        txtBadge.setTextColor(badgeColor);
        bg.setColor(badgeBgColor);
        txtBadge.setBackground(bg);
        row.addView(txtBadge);

        return row;
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