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

        // Perform analysis using authoritative backend RiskScoreModel
        performRiskAnalysis(permissions);
    }

    private void performRiskAnalysis(List<String> permissions) {
        // Render requested permissions list
        renderPermissionsList(permissions);

        // Check if authoritative RiskScoreModel was already computed & cached by backend
        com.example.securedroid.utils.SessionManager sessionManager = com.example.securedroid.utils.SessionManager.getInstance(this);
        RiskScoreModel cachedModel = sessionManager.getAppRiskScore(packageName);
        if (cachedModel != null) {
            applyRiskScoreModel(cachedModel, permissions);
        } else {
            showLoadingState();
        }

        // Query backend risk engine (calculate_privacy_risk)
        try {
            AppAnalysisRequest req = new AppAnalysisRequest(packageName, appName, permissions);
            ApiClient.getAnalysisApi(this).analyzeApplication(req).enqueue(new Callback<RiskScoreModel>() {
                @Override
                public void onResponse(Call<RiskScoreModel> call, Response<RiskScoreModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RiskScoreModel model = response.body();
                        sessionManager.saveAppRiskScore(packageName, model);
                        applyRiskScoreModel(model, permissions);
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