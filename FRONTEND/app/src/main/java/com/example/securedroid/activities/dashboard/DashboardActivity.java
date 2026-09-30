package com.example.securedroid.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;
import com.example.securedroid.activities.AlertDetailsActivity;
import com.example.securedroid.activities.NotificationHistoryActivity;
import com.example.securedroid.activities.PrivacyReportActivity;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.DashboardSummaryResponse;
import com.example.securedroid.fragments.AppsFragment;
import com.example.securedroid.fragments.LiveMonitorFragment;
import com.example.securedroid.fragments.ProfileFragment;
import com.example.securedroid.fragments.WebsiteFragment;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.repository.DashboardRepository;
import com.example.securedroid.services.PrivacyMonitorService;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting, txtUserName;
    private TextView txtRiskScore, txtRiskLevel, txtLastScanTime, txtSecurityStatusSummary;
    private FrameLayout layoutScoreCircle;
    private MaterialButton btnAnalyze;
    private BottomNavigationView bottomNavigation;
    private View dashboardScroll;
    private FrameLayout fragmentContainer;
    private View layoutNotifications;

    private View cardApps, cardWebsite, cardAlerts, cardSafeApps;
    private View cardInstalledApps, cardWebsiteAnalysis, cardHistory, cardAIAdvisor;

    // AI Recommendation & Recent Alerts
    private TextView txtAiRecommendationTitle, txtAiRecommendationDesc;
    private Button btnTakeAction;
    private TextView txtViewAllAlerts;
    private View cardRecentAlert1, cardRecentAlert2;

    private DashboardRepository dashboardRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = SessionManager.getInstance(this);
        dashboardRepository = new DashboardRepository();

        initViews();
        setupGreeting();
        setupQuickActions();
        loadDashboardMetrics();
        setupBottomNavigation();

        try {
            Intent serviceIntent = new Intent(this, PrivacyMonitorService.class);
            startService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        dashboardScroll = findViewById(R.id.dashboardScroll);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserName = findViewById(R.id.txtUserName);

        txtRiskScore = findViewById(R.id.txtRiskScore);
        txtRiskLevel = findViewById(R.id.txtRiskLevel);
        layoutScoreCircle = findViewById(R.id.layoutScoreCircle);
        txtLastScanTime = findViewById(R.id.txtLastScanTime);
        txtSecurityStatusSummary = findViewById(R.id.txtSecurityStatusSummary);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        layoutNotifications = findViewById(R.id.layoutNotifications);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Statistic Cards
        cardApps = findViewById(R.id.cardApps);
        cardWebsite = findViewById(R.id.cardWebsite);
        cardAlerts = findViewById(R.id.cardAlerts);
        cardSafeApps = findViewById(R.id.cardSafeApps);

        // Quick Actions Cards
        cardInstalledApps = findViewById(R.id.cardInstalledApps);
        cardWebsiteAnalysis = findViewById(R.id.cardWebsiteAnalysis);
        cardHistory = findViewById(R.id.cardHistory);
        cardAIAdvisor = findViewById(R.id.cardAIAdvisor);

        // AI Recommendation Views
        txtAiRecommendationTitle = findViewById(R.id.txtAiRecommendationTitle);
        txtAiRecommendationDesc = findViewById(R.id.txtAiRecommendationDesc);
        btnTakeAction = findViewById(R.id.btnTakeAction);

        // Recent Alerts Views
        txtViewAllAlerts = findViewById(R.id.txtViewAllAlerts);
        cardRecentAlert1 = findViewById(R.id.cardRecentAlert1);
        cardRecentAlert2 = findViewById(R.id.cardRecentAlert2);

        // Notification Icon Click Listener
        if (layoutNotifications != null) {
            layoutNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, NotificationHistoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> {
                updateScanTimestamp();
                loadDashboardMetrics();
                Toast.makeText(this, "Scanning device applications & privacy status...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnTakeAction != null) {
            btnTakeAction.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Installed Applications audit...", Toast.LENGTH_SHORT).show();
                if (bottomNavigation != null) {
                    bottomNavigation.setSelectedItemId(R.id.nav_apps);
                }
            });
        }

        if (txtViewAllAlerts != null) {
            txtViewAllAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, NotificationHistoryActivity.class);
                startActivity(intent);
            });
        }

        // Restore saved last scan time if available
        String savedTime = sessionManager.getLastScanTime();
        if (savedTime != null && txtLastScanTime != null) {
            txtLastScanTime.setText(savedTime);
        } else {
            updateScanTimestamp();
        }
    }

    private void updateScanTimestamp() {
        String timeStr = "Last Scan : " + new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
        if (txtLastScanTime != null) {
            txtLastScanTime.setText(timeStr);
        }
        if (sessionManager != null) {
            sessionManager.saveLastScanTime(timeStr);
        }
    }

    private void setupGreeting() {
        if (txtGreeting != null && sessionManager != null) {
            String name = sessionManager.getUserName();
            if (name != null && !name.isEmpty()) {
                txtGreeting.setText("Hello, " + name);
            } else {
                txtGreeting.setText("Hello, User");
            }
        }
        if (txtUserName != null) {
            txtUserName.setText("Security Dashboard");
        }
    }

    private void setupQuickActions() {
        // Quick Action 1: Installed Apps
        if (cardInstalledApps != null) {
            TextView txt = cardInstalledApps.findViewById(R.id.txtQuickAction);
            ImageView img = cardInstalledApps.findViewById(R.id.imgQuickAction);
            if (txt != null) txt.setText("Installed Apps");
            if (img != null) img.setImageResource(R.drawable.ic_apps);
            cardInstalledApps.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
            });
        }

        // Quick Action 2: Website Analysis
        if (cardWebsiteAnalysis != null) {
            TextView txt = cardWebsiteAnalysis.findViewById(R.id.txtQuickAction);
            ImageView img = cardWebsiteAnalysis.findViewById(R.id.imgQuickAction);
            if (txt != null) txt.setText("Website Scanner");
            if (img != null) img.setImageResource(R.drawable.ic_language);
            cardWebsiteAnalysis.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_reports);
            });
        }

        // Quick Action 3: Privacy Report / History
        if (cardHistory != null) {
            TextView txt = cardHistory.findViewById(R.id.txtQuickAction);
            ImageView img = cardHistory.findViewById(R.id.imgQuickAction);
            if (txt != null) txt.setText("Privacy Report");
            if (img != null) img.setImageResource(R.drawable.ic_reports);
            cardHistory.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, PrivacyReportActivity.class);
                startActivity(intent);
            });
        }

        // Quick Action 4: High Risk / AI Advisor
        if (cardAIAdvisor != null) {
            TextView txt = cardAIAdvisor.findViewById(R.id.txtQuickAction);
            ImageView img = cardAIAdvisor.findViewById(R.id.imgQuickAction);
            if (txt != null) txt.setText("High Risk Alerts");
            if (img != null) {
                img.setImageResource(R.drawable.ic_warning);
                img.setColorFilter(0xFFFF3B30);
            }
            cardAIAdvisor.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                intent.putExtra("APP_NAME", "Suspicious App Risk");
                intent.putExtra("PERMISSION", "Camera & Location Access");
                startActivity(intent);
            });
        }

        // Statistics Cards Click Actions
        if (cardApps != null) {
            cardApps.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
            });
        }
        if (cardWebsite != null) {
            cardWebsite.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_reports);
            });
        }
        if (cardAlerts != null) {
            cardAlerts.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
            });
        }
        if (cardSafeApps != null) {
            cardSafeApps.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
            });
        }
    }

    private void loadDashboardMetrics() {
        ApiClient.getDashboardApi(this).getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardSummaryResponse summary = response.body();
                    updateUI(summary);
                } else {
                    calculateLiveDeviceMetrics();
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                calculateLiveDeviceMetrics();
            }
        });
    }

    private void updateUI(DashboardSummaryResponse summary) {
        // Calculate based on live phone apps to guarantee consistency with Apps list
        calculateLiveDeviceMetrics();
    }

    private void calculateLiveDeviceMetrics() {
        List<AppModel> apps = PackageManagerHelper.getInstalledApps(this);
        int totalApps = apps.size();
        int highRiskApps = 0;
        int mediumRiskApps = 0;
        int safeApps = 0;

        List<AppModel> highRiskList = new ArrayList<>();
        List<AppModel> mediumRiskList = new ArrayList<>();

        for (AppModel app : apps) {
            boolean hasCamera = false;
            boolean hasMic = false;
            boolean hasLocation = false;
            boolean hasSms = false;

            if (app.getPermissions() != null) {
                for (String p : app.getPermissions()) {
                    String pUpper = p.toUpperCase();
                    if (pUpper.contains("CAMERA")) hasCamera = true;
                    if (pUpper.contains("RECORD_AUDIO") || pUpper.contains("MICROPHONE")) hasMic = true;
                    if (pUpper.contains("LOCATION")) hasLocation = true;
                    if (pUpper.contains("SMS") || pUpper.contains("CONTACT")) hasSms = true;
                }
            }

            if ((hasCamera && hasLocation) || (hasCamera && hasMic) || (hasSms && hasLocation)) {
                highRiskApps++;
                highRiskList.add(app);
            } else if (hasCamera || hasMic || hasLocation || hasSms) {
                mediumRiskApps++;
                mediumRiskList.add(app);
            } else {
                safeApps++;
            }
        }

        // Privacy Score calculation: Higher is safer
        int score = totalApps > 0 ? Math.max(15, 100 - (highRiskApps * 10) - (mediumRiskApps * 3)) : 85;
        String level = score >= 70 ? "LOW RISK" : (score >= 40 ? "MEDIUM RISK" : "HIGH RISK");

        applyRiskScoreStyling(score, level);

        // Update Stat Cards with accurate device counts
        if (cardApps != null) {
            ((TextView) cardApps.findViewById(R.id.txtValue)).setText(String.valueOf(totalApps));
            ((TextView) cardApps.findViewById(R.id.txtTitle)).setText("Total Apps");
        }
        if (cardWebsite != null) {
            ((TextView) cardWebsite.findViewById(R.id.txtValue)).setText(String.valueOf(mediumRiskApps));
            ((TextView) cardWebsite.findViewById(R.id.txtTitle)).setText("Medium Risk");
        }
        if (cardAlerts != null) {
            ((TextView) cardAlerts.findViewById(R.id.txtValue)).setText(String.valueOf(highRiskApps));
            ((TextView) cardAlerts.findViewById(R.id.txtTitle)).setText("High Risk");
            ((TextView) cardAlerts.findViewById(R.id.txtValue)).setTextColor(0xFFFF3B30);
        }
        if (cardSafeApps != null) {
            ((TextView) cardSafeApps.findViewById(R.id.txtValue)).setText(String.valueOf(safeApps));
            ((TextView) cardSafeApps.findViewById(R.id.txtTitle)).setText("Safe Apps");
            ((TextView) cardSafeApps.findViewById(R.id.txtValue)).setTextColor(0xFF00E676);
        }

        // Update AI Recommendation description dynamically
        if (txtAiRecommendationDesc != null) {
            if (highRiskApps > 0) {
                txtAiRecommendationDesc.setText(highRiskApps + " apps have sensitive permissions (Camera, Location, SMS). Review permissions to protect your privacy.");
            } else if (mediumRiskApps > 0) {
                txtAiRecommendationDesc.setText(mediumRiskApps + " apps have single sensitive permissions (Mic/Location). Audit apps to verify background usage.");
            } else {
                txtAiRecommendationDesc.setText("All scanned applications follow baseline security guidelines. Background protection active.");
            }
        }

        // Update Recent Alerts cards dynamically with real high/medium risk apps
        setupRecentAlertCards(highRiskList, mediumRiskList);
    }

    private void setupRecentAlertCards(List<AppModel> highRisk, List<AppModel> mediumRisk) {
        // Alert 1
        if (cardRecentAlert1 != null) {
            TextView txtTitle1 = cardRecentAlert1.findViewById(R.id.txtAlertTitle);
            TextView txtSub1 = cardRecentAlert1.findViewById(R.id.txtAlertSubtitle);
            ImageView img1 = cardRecentAlert1.findViewById(R.id.imgAlert);

            if (!highRisk.isEmpty()) {
                AppModel app1 = highRisk.get(0);
                if (txtTitle1 != null) txtTitle1.setText(app1.getApplicationName() + " - High Risk Permissions");
                if (txtSub1 != null) txtSub1.setText(app1.getApplicationName() + " • Camera & Location • Today");
                if (img1 != null) img1.setColorFilter(0xFFFF3B30);

                cardRecentAlert1.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app1.getApplicationName());
                    intent.putExtra("PERMISSION", "Camera & Location Access");
                    intent.putExtra("RISK_LEVEL", "High");
                    intent.putExtra("DESCRIPTION", "This app has simultaneous access to multiple sensitive permissions.");
                    startActivity(intent);
                });
            } else {
                if (txtTitle1 != null) txtTitle1.setText("Camera Permission Access");
                if (txtSub1 != null) txtSub1.setText("Background Inspection • Today");
                if (img1 != null) img1.setColorFilter(0xFFFF9800);

                cardRecentAlert1.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", "System Camera Monitor");
                    intent.putExtra("PERMISSION", "Camera Sensor");
                    startActivity(intent);
                });
            }
        }

        // Alert 2
        if (cardRecentAlert2 != null) {
            TextView txtTitle2 = cardRecentAlert2.findViewById(R.id.txtAlertTitle);
            TextView txtSub2 = cardRecentAlert2.findViewById(R.id.txtAlertSubtitle);
            ImageView img2 = cardRecentAlert2.findViewById(R.id.imgAlert);

            if (highRisk.size() > 1) {
                AppModel app2 = highRisk.get(1);
                if (txtTitle2 != null) txtTitle2.setText(app2.getApplicationName() + " - Sensitive Access");
                if (txtSub2 != null) txtSub2.setText(app2.getApplicationName() + " • Microphone & Storage • Today");
                if (img2 != null) img2.setColorFilter(0xFFFF3B30);

                cardRecentAlert2.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app2.getApplicationName());
                    intent.putExtra("PERMISSION", "Microphone & Storage");
                    intent.putExtra("RISK_LEVEL", "High");
                    intent.putExtra("DESCRIPTION", "Application requests microphone and storage access in background.");
                    startActivity(intent);
                });
            } else if (!mediumRisk.isEmpty()) {
                AppModel app2 = mediumRisk.get(0);
                if (txtTitle2 != null) txtTitle2.setText(app2.getApplicationName() + " - Location Access");
                if (txtSub2 != null) txtSub2.setText(app2.getApplicationName() + " • Approximate Location • Today");
                if (img2 != null) img2.setColorFilter(0xFFFFC107);

                cardRecentAlert2.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app2.getApplicationName());
                    intent.putExtra("PERMISSION", "Approximate Location");
                    intent.putExtra("RISK_LEVEL", "Medium");
                    intent.putExtra("DESCRIPTION", "Application accesses device location coordinates.");
                    startActivity(intent);
                });
            } else {
                if (txtTitle2 != null) txtTitle2.setText("Microphone Access Detected");
                if (txtSub2 != null) txtSub2.setText("Audio Stream • Today");
                if (img2 != null) img2.setColorFilter(0xFFFFC107);

                cardRecentAlert2.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", "Audio Monitor");
                    intent.putExtra("PERMISSION", "Microphone");
                    startActivity(intent);
                });
            }
        }
    }

    private void applyRiskScoreStyling(int score, String level) {
        if (txtRiskScore != null) {
            txtRiskScore.setText(String.valueOf(score));
        }

        int targetColor;
        if (score >= 70 || (level != null && level.contains("LOW"))) {
            targetColor = 0xFF00E676; // Vibrant Green
            if (txtRiskLevel != null) {
                txtRiskLevel.setText("LOW RISK (PROTECTED)");
            }
            if (txtSecurityStatusSummary != null) {
                txtSecurityStatusSummary.setText("Your device appears secure. Privacy score is healthy.");
            }
        } else if (score >= 40 || (level != null && level.contains("MEDIUM"))) {
            targetColor = 0xFFFFC107; // Vibrant Amber/Yellow
            if (txtRiskLevel != null) {
                txtRiskLevel.setText("MEDIUM RISK");
            }
            if (txtSecurityStatusSummary != null) {
                txtSecurityStatusSummary.setText("Moderate risk detected. Audit apps with location/mic access.");
            }
        } else {
            targetColor = 0xFFFF3B30; // Vibrant Red
            if (txtRiskLevel != null) {
                txtRiskLevel.setText("HIGH RISK DETECTED");
            }
            if (txtSecurityStatusSummary != null) {
                txtSecurityStatusSummary.setText("High privacy risk detected! Review sensitive app permissions.");
            }
        }

        if (txtRiskScore != null) {
            txtRiskScore.setTextColor(targetColor);
        }
        if (txtRiskLevel != null) {
            txtRiskLevel.setTextColor(targetColor);
        }

        // Apply matching dynamic color to the circular ring around the risk score
        if (layoutScoreCircle != null) {
            android.graphics.drawable.GradientDrawable circleBg = new android.graphics.drawable.GradientDrawable();
            circleBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circleBg.setColor(0xFF1B2B40);
            int strokeWidthPx = (int) (8 * getResources().getDisplayMetrics().density);
            circleBg.setStroke(strokeWidthPx, targetColor);
            layoutScoreCircle.setBackground(circleBg);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    if (dashboardScroll != null) dashboardScroll.setVisibility(View.VISIBLE);
                    if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
                    return true;
                } else if (id == R.id.nav_monitor) {
                    loadFragment(new LiveMonitorFragment());
                    return true;
                } else if (id == R.id.nav_apps) {
                    loadFragment(new AppsFragment());
                    return true;
                } else if (id == R.id.nav_reports) {
                    loadFragment(new WebsiteFragment());
                    return true;
                } else if (id == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }
                return false;
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        if (dashboardScroll != null) dashboardScroll.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}