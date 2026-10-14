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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securedroid.R;
import com.example.securedroid.fragments.AppsFragment;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.models.RiskScoreModel;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, imgRiskIcon, imgAppIcon;
    private Button btnRestrictApp, btnViewAppAnalysis, btnIgnore;
    private MaterialCardView cardAlertDetails;
    private LinearLayout layoutRiskBadge;
    private TextView txtRiskBadge, txtAlertTitle, txtAppName, txtPackageName, txtPermission, txtTime, txtReason, txtAiRecommendation;
    private String packageName;
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRestrictApp = findViewById(R.id.btnRestrictApp);
        btnViewAppAnalysis = findViewById(R.id.btnViewAppAnalysis);
        btnIgnore = findViewById(R.id.btnIgnore);

        cardAlertDetails = findViewById(R.id.cardAlertDetails);
        layoutRiskBadge = findViewById(R.id.layoutRiskBadge);
        imgRiskIcon = findViewById(R.id.imgRiskIcon);
        txtRiskBadge = findViewById(R.id.txtRiskBadge);

        txtAlertTitle = findViewById(R.id.txtAlertTitle);
        imgAppIcon = findViewById(R.id.imgAppIcon);
        txtAppName = findViewById(R.id.txtAppName);
        txtPackageName = findViewById(R.id.txtPackageName);
        txtPermission = findViewById(R.id.txtPermission);
        txtTime = findViewById(R.id.txtTime);
        txtReason = findViewById(R.id.txtReason);
        txtAiRecommendation = findViewById(R.id.txtAiRecommendation);
    }

    private void bindData() {
        Intent intent = getIntent();
        if (intent == null) return;

        appName = intent.getStringExtra("APP_NAME");
        packageName = intent.getStringExtra("PACKAGE_NAME");
        if (packageName == null || packageName.isEmpty()) {
            packageName = intent.getStringExtra("package_name");
        }

        String perm = intent.getStringExtra("PERMISSION");
        String time = intent.getStringExtra("TIME");
        String reason = intent.getStringExtra("DESCRIPTION");
        String currRisk = intent.getStringExtra("CURR_RISK");
        if (currRisk == null || currRisk.isEmpty()) {
            currRisk = intent.getStringExtra("RISK_LEVEL");
        }
        String alertTitle = intent.getStringExtra("ALERT_TITLE");

        PackageManager pm = getPackageManager();

        // 1. Resolve package name if missing
        if ((packageName == null || packageName.isEmpty()) && appName != null && !appName.isEmpty()
                && !"App".equalsIgnoreCase(appName) && !"Suspicious App Risk".equalsIgnoreCase(appName)) {
            try {
                List<PackageInfo> installed = pm.getInstalledPackages(0);
                for (PackageInfo pi : installed) {
                    CharSequence label = pm.getApplicationLabel(pi.applicationInfo);
                    if (appName.equalsIgnoreCase(label.toString())) {
                        packageName = pi.packageName;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Fetch app icon & verified label
        if (packageName != null && !packageName.isEmpty()) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                CharSequence verifiedLabel = pm.getApplicationLabel(ai);
                if (appName == null || appName.isEmpty() || "App".equalsIgnoreCase(appName) || "Suspicious App Risk".equalsIgnoreCase(appName)) {
                    appName = verifiedLabel.toString();
                }
                Drawable icon = pm.getApplicationIcon(ai);
                if (imgAppIcon != null) {
                    imgAppIcon.setImageDrawable(icon);
                    imgAppIcon.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {}
        }

        if (txtAppName != null) {
            txtAppName.setText(appName != null && !appName.isEmpty() ? appName : "Application");
        }
        if (txtPackageName != null) {
            if (packageName != null && !packageName.isEmpty()) {
                txtPackageName.setText(packageName);
                txtPackageName.setVisibility(View.VISIBLE);
            } else {
                txtPackageName.setVisibility(View.GONE);
            }
        }

        // 3. Resolve permissions
        boolean isGenericPerm = (perm == null || perm.isEmpty()
                || "Camera & Location Access".equalsIgnoreCase(perm)
                || "Location & Storage Access".equalsIgnoreCase(perm)
                || "Microphone & Storage".equalsIgnoreCase(perm)
                || "Approximate Location".equalsIgnoreCase(perm)
                || "Camera".equalsIgnoreCase(perm)
                || "Permission Alert".equalsIgnoreCase(perm));

        if (packageName != null && !packageName.isEmpty() && isGenericPerm) {
            AppModel appModel = PackageManagerHelper.getAppDetails(this, packageName);
            if (appModel != null) {
                List<String> sens = appModel.getGrantedSensitivePermissions();
                if (sens != null && !sens.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < sens.size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(PackageManagerHelper.getPermissionDisplayName(sens.get(i)));
                    }
                    perm = sb.toString();
                } else if (appModel.getDeclaredPermissions() != null && !appModel.getDeclaredPermissions().isEmpty()) {
                    perm = appModel.getDeclaredPermissions().size() + " Declared Permissions";
                } else {
                    perm = "Standard Permissions";
                }
            }
        }

        if (txtPermission != null) {
            txtPermission.setText(perm != null && !perm.isEmpty() ? perm : "Standard Permissions");
        }

        // 4. Resolve Title
        if (txtAlertTitle != null) {
            if (alertTitle != null && !alertTitle.isEmpty() && !"Permission Alert".equalsIgnoreCase(alertTitle)) {
                txtAlertTitle.setText(alertTitle);
            } else {
                txtAlertTitle.setText((appName != null ? appName : "App") + " - Privacy Audit");
            }
        }

        // 5. Resolve Time
        if (time == null || time.isEmpty() || "Today, 10:30 AM".equalsIgnoreCase(time)
                || "Today".equalsIgnoreCase(time) || "Recent".equalsIgnoreCase(time)) {
            if (packageName != null && !packageName.isEmpty()) {
                try {
                    PackageInfo pi = pm.getPackageInfo(packageName, 0);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
                    time = sdf.format(new Date(pi.lastUpdateTime));
                } catch (Exception ignored) {
                    time = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
                }
            } else {
                time = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
            }
        }
        if (txtTime != null) txtTime.setText(time);

        // 6. Resolve Reason / Description
        boolean isGenericReason = (reason == null || reason.isEmpty()
                || reason.contains("without active UI session")
                || reason.contains("simultaneous access to multiple")
                || reason.contains("requests location or storage")
                || reason.contains("requests microphone and storage"));

        SessionManager sessionManager = new SessionManager(this);

        if (packageName != null && !packageName.isEmpty() && isGenericReason) {
            RiskScoreModel rsm = sessionManager.getAppRiskScore(packageName);
            if (rsm != null && rsm.getExplanations() != null && !rsm.getExplanations().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String ex : rsm.getExplanations()) {
                    if (sb.length() > 0) sb.append("\n• ");
                    else sb.append("• ");
                    sb.append(ex);
                }
                reason = sb.toString();
            } else if (rsm != null && rsm.getAnalysisSummary() != null && !rsm.getAnalysisSummary().isEmpty()) {
                reason = rsm.getAnalysisSummary();
            } else {
                AppModel appModel = PackageManagerHelper.getAppDetails(this, packageName);
                if (appModel != null && appModel.getGrantedSensitivePermissions() != null && !appModel.getGrantedSensitivePermissions().isEmpty()) {
                    reason = (appName != null ? appName : "This app") + " is granted " +
                            appModel.getGrantedSensitivePermissions().size() +
                            " sensitive runtime permission(s) (" + (perm != null ? perm : "") +
                            ") capable of accessing device sensors or user privacy data.";
                } else {
                    reason = (appName != null ? appName : "This app") + " utilizes standard Android platform capabilities with baseline risk.";
                }
            }
        }

        if (txtReason != null) {
            txtReason.setText(reason != null && !reason.isEmpty() ? reason : "Standard application profile.");
        }

        // 7. Resolve Risk Level
        if (currRisk == null || currRisk.isEmpty()) {
            if (packageName != null && !packageName.isEmpty()) {
                RiskScoreModel rsm = sessionManager.getAppRiskScore(packageName);
                if (rsm != null && rsm.getRiskLevel() != null) {
                    currRisk = rsm.getRiskLevel();
                } else {
                    AppModel appModel = PackageManagerHelper.getAppDetails(this, packageName);
                    if (appModel != null) {
                        currRisk = AppsFragment.getAppRiskLevel(this, appModel);
                    }
                }
            }
        }
        if (currRisk == null || currRisk.isEmpty()) {
            currRisk = "LOW";
        }

        // 8. Style Risk Badge
        applyRiskBadgeStyling(currRisk);

        // 9. AI Recommendation
        if (txtAiRecommendation != null) {
            String upper = currRisk.toUpperCase();
            if (upper.contains("HIGH") || upper.contains("CRITICAL")) {
                if (perm != null && perm.toLowerCase().contains("location")) {
                    txtAiRecommendation.setText("We recommend changing Location permission to 'While using app' or revoking background location in Android Settings.");
                } else if (perm != null && (perm.toLowerCase().contains("camera") || perm.toLowerCase().contains("microphone"))) {
                    txtAiRecommendation.setText("We recommend restricting camera and microphone access when this app is not actively in use.");
                } else {
                    txtAiRecommendation.setText("We recommend reviewing and revoking unnecessary sensitive permissions in Android Settings.");
                }
            } else if (upper.contains("MEDIUM")) {
                txtAiRecommendation.setText("Review background permission access in App Settings to ensure compliance with your privacy expectations.");
            } else {
                txtAiRecommendation.setText("This application conforms to secure permission practices. No immediate privacy restriction is required.");
            }
        }
    }

    private void applyRiskBadgeStyling(String risk) {
        if (risk == null) risk = "LOW";
        String upper = risk.toUpperCase();

        int textColor;
        int badgeBgColor;
        int strokeColor;
        int cardBgColor;
        int iconRes = R.drawable.ic_warning;
        String badgeText;

        if (upper.contains("HIGH") || upper.contains("CRITICAL")) {
            textColor = 0xFFFF3B30;
            badgeBgColor = 0x2EFF3B30;
            strokeColor = 0xFF521525;
            cardBgColor = 0xFF1A101C;
            badgeText = "High Risk";
        } else if (upper.contains("MEDIUM")) {
            textColor = 0xFFFFC107;
            badgeBgColor = 0x2EFFC107;
            strokeColor = 0xFF544410;
            cardBgColor = 0xFF1D1B10;
            badgeText = "Medium Risk";
        } else {
            textColor = 0xFF00E676;
            badgeBgColor = 0x2E00E676;
            strokeColor = 0xFF103D20;
            cardBgColor = 0xFF0E1B13;
            iconRes = R.drawable.ic_shield;
            badgeText = "Low Risk";
        }

        if (txtRiskBadge != null) {
            txtRiskBadge.setText(badgeText);
            txtRiskBadge.setTextColor(textColor);
        }
        if (imgRiskIcon != null) {
            imgRiskIcon.setImageResource(iconRes);
            imgRiskIcon.setColorFilter(textColor);
        }
        if (layoutRiskBadge != null) {
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(16 * getResources().getDisplayMetrics().density);
            badgeBg.setColor(badgeBgColor);
            layoutRiskBadge.setBackground(badgeBg);
        }
        if (cardAlertDetails != null) {
            cardAlertDetails.setStrokeColor(strokeColor);
            cardAlertDetails.setCardBackgroundColor(cardBgColor);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnIgnore.setOnClickListener(v -> {
            Toast.makeText(this, "Alert marked as reviewed.", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnRestrictApp.setOnClickListener(v -> openAppSettings());

        if (btnViewAppAnalysis != null) {
            btnViewAppAnalysis.setOnClickListener(v -> openAppAnalysis());
        }
    }

    private void openAppSettings() {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Android Application Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAppAnalysis() {
        if (packageName != null && !packageName.isEmpty()) {
            Intent intent = new Intent(this, AppDetailsActivity.class);
            intent.putExtra("PACKAGE_NAME", packageName);
            intent.putExtra("package_name", packageName);
            intent.putExtra("APP_NAME", appName != null ? appName : packageName);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Package information unavailable for deep analysis", Toast.LENGTH_SHORT).show();
        }
    }
}
