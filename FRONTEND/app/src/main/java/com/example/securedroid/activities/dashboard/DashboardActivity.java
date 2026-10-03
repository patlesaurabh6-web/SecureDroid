package com.example.securedroid.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
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
import com.example.securedroid.activities.PrivacyHistoryActivity;
import com.example.securedroid.activities.PrivacyReportActivity;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.DashboardSummaryResponse;
import com.example.securedroid.fragments.AppsFragment;
import com.example.securedroid.fragments.LiveMonitorFragment;
import com.example.securedroid.fragments.ProfileFragment;
import com.example.securedroid.fragments.WebsiteFragment;
import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.models.RiskScoreModel;
import com.example.securedroid.models.WebsiteModel;
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

    private ImageView imgScanRadar;
    private android.widget.ProgressBar progressScanAudit;
    private View layoutScoreText;

    private View cardApps, cardWebsite, cardAlerts, cardSafeApps;
    private View cardInstalledApps, cardWebsiteAnalysis, cardHistory, cardAIAdvisor;

    // AI Recommendation & Recent Alerts
    private TextView txtAiRecommendationTitle, txtAiRecommendationDesc;
    private Button btnTakeAction;
    private TextView txtViewAllAlerts;
    private View cardRecentAlert1, cardRecentAlert2;

    // Privacy Timeline
    private View cardTimelineItem1, cardTimelineItem2;

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

        imgScanRadar = findViewById(R.id.imgScanRadar);
        progressScanAudit = findViewById(R.id.progressScanAudit);
        layoutScoreText = findViewById(R.id.layoutScoreText);

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

        // Privacy Timeline Views
        cardTimelineItem1 = findViewById(R.id.cardTimelineItem1);
        cardTimelineItem2 = findViewById(R.id.cardTimelineItem2);

        // Notification Icon Click Listener
        if (layoutNotifications != null) {
            layoutNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, NotificationHistoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> startScanningProcessAnimation());
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

    private void startScanningProcessAnimation() {
        if (btnAnalyze != null) {
            btnAnalyze.setEnabled(false);
            btnAnalyze.setText("Scanning Applications...");
        }

        if (txtSecurityStatusSummary != null) {
            txtSecurityStatusSummary.setText("Auditing installed applications & permissions...");
        }

        if (progressScanAudit != null) {
            progressScanAudit.setVisibility(View.VISIBLE);
        }

        if (imgScanRadar != null) {
            imgScanRadar.setVisibility(View.VISIBLE);
            RotateAnimation rotateAnimation = new RotateAnimation(
                    0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(1200);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            imgScanRadar.startAnimation(rotateAnimation);
        }

        if (layoutScoreText != null) {
            layoutScoreText.animate().alpha(0.25f).setDuration(300).start();
        }

        if (layoutScoreCircle != null) {
            layoutScoreCircle.animate().scaleX(1.04f).scaleY(1.04f).setDuration(400).start();
        }

        // Dispatch installed apps to authoritative backend risk engine (calculate_privacy_risk)
        List<AppModel> installedList = PackageManagerHelper.getInstalledApps(this);
        for (AppModel app : installedList) {
            com.example.securedroid.api.dto.AppAnalysisRequest req = new com.example.securedroid.api.dto.AppAnalysisRequest(
                    app.getPackageName(),
                    app.getApplicationName(),
                    app.getVersionName(),
                    app.getDeveloper(),
                    app.getPermissions()
            );
            ApiClient.getAnalysisApi(this).analyzeApplication(req).enqueue(new Callback<RiskScoreModel>() {
                @Override
                public void onResponse(Call<RiskScoreModel> call, Response<RiskScoreModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        sessionManager.saveAppRiskScore(app.getPackageName(), response.body());
                    }
                }

                @Override
                public void onFailure(Call<RiskScoreModel> call, Throwable t) {}
            });
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            updateScanTimestamp();
            loadDashboardMetrics();

            if (imgScanRadar != null) {
                imgScanRadar.clearAnimation();
                imgScanRadar.setVisibility(View.GONE);
            }

            if (progressScanAudit != null) {
                progressScanAudit.setVisibility(View.GONE);
            }

            if (layoutScoreText != null) {
                layoutScoreText.animate().alpha(1.0f).setDuration(300).start();
            }

            if (layoutScoreCircle != null) {
                layoutScoreCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
            }

            if (btnAnalyze != null) {
                btnAnalyze.setEnabled(true);
                btnAnalyze.setText("Analyze Now");
            }

            Toast.makeText(DashboardActivity.this, "Scan complete: Security audit updated", Toast.LENGTH_SHORT).show();
        }, 1800);
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
                loadFragment(AppsFragment.newInstance("ALL"));
            });
        }
        if (cardWebsite != null) {
            cardWebsite.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
                loadFragment(AppsFragment.newInstance("MEDIUM"));
            });
        }
        if (cardAlerts != null) {
            cardAlerts.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
                loadFragment(AppsFragment.newInstance("HIGH"));
            });
        }
        if (cardSafeApps != null) {
            cardSafeApps.setOnClickListener(v -> {
                if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
                loadFragment(AppsFragment.newInstance("SAFE"));
            });
        }
    }

    private void loadDashboardMetrics() {
        ApiClient.getDashboardApi(this).getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    loadCachedDeviceMetrics();
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                loadCachedDeviceMetrics();
            }
        });
    }

    private void updateUI(DashboardSummaryResponse summary) {
        if (summary == null) {
            loadCachedDeviceMetrics();
            return;
        }

        int score = Math.round(summary.getOverallRiskScore());
        String level = summary.getRiskLevel() != null ? summary.getRiskLevel().toUpperCase() : "LOW";

        applyRiskScoreStyling(score, level);

        List<AppModel> apps = PackageManagerHelper.getInstalledApps(this);
        int totalApps = apps.size();
        int safeApps = 0;
        List<AppModel> highRiskList = new ArrayList<>();
        List<AppModel> mediumRiskList = new ArrayList<>();
        for (AppModel app : apps) {
            String r = AppsFragment.getAppRiskLevel(this, app);
            if ("HIGH".equalsIgnoreCase(r) || "CRITICAL".equalsIgnoreCase(r)) {
                highRiskList.add(app);
            } else if ("MEDIUM".equalsIgnoreCase(r)) {
                mediumRiskList.add(app);
            } else {
                safeApps++;
            }
        }
        int highRiskApps = highRiskList.size();
        int mediumRiskApps = mediumRiskList.size();

        // Update Stat Cards with actual device app counts matching AppsFragment sections
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

        // Update AI Recommendation description dynamically from authoritative backend
        if (txtAiRecommendationTitle != null && summary.getRecommendationTitle() != null) {
            txtAiRecommendationTitle.setText(summary.getRecommendationTitle());
        }
        if (txtAiRecommendationDesc != null && summary.getRecommendationSummary() != null) {
            txtAiRecommendationDesc.setText(summary.getRecommendationSummary());
        }

        // Update Recent Alerts cards dynamically
        setupRecentAlertCards(highRiskList, mediumRiskList, totalApps);

        // Update Privacy Timeline dynamically
        setupPrivacyTimeline(score, level, totalApps);
    }

    private void loadCachedDeviceMetrics() {
        List<AppModel> apps = PackageManagerHelper.getInstalledApps(this);
        int totalApps = apps.size();
        int highRiskApps = 0;
        int mediumRiskApps = 0;
        int safeApps = 0;
        float scoreSum = 0;
        int scoredCount = 0;

        List<AppModel> highRiskList = new ArrayList<>();
        List<AppModel> mediumRiskList = new ArrayList<>();

        for (AppModel app : apps) {
            RiskScoreModel model = sessionManager != null ? sessionManager.getAppRiskScore(app.getPackageName()) : null;
            if (model != null) {
                scoreSum += model.getRiskScore();
                scoredCount++;
            }
            String r = AppsFragment.getAppRiskLevel(this, app);
            if ("HIGH".equalsIgnoreCase(r) || "CRITICAL".equalsIgnoreCase(r)) {
                highRiskApps++;
                highRiskList.add(app);
            } else if ("MEDIUM".equalsIgnoreCase(r)) {
                mediumRiskApps++;
                mediumRiskList.add(app);
            } else {
                safeApps++;
            }
        }

        int score = scoredCount > 0 ? Math.round(scoreSum / scoredCount) : 15;
        String level = score <= 30 ? "LOW RISK" : (score <= 60 ? "MEDIUM RISK" : "HIGH RISK");

        applyRiskScoreStyling(score, level);

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

        setupRecentAlertCards(highRiskList, mediumRiskList, totalApps);
        setupPrivacyTimeline(score, level, totalApps);
    }

    private void setupRecentAlertCards(List<AppModel> highRisk, List<AppModel> mediumRisk, int totalApps) {
        List<AlertModel> savedAlerts = sessionManager != null ? sessionManager.getPrivacyAlerts() : null;

        // Card 1
        if (cardRecentAlert1 != null) {
            TextView txtTitle1 = cardRecentAlert1.findViewById(R.id.txtAlertTitle);
            TextView txtSub1 = cardRecentAlert1.findViewById(R.id.txtAlertSubtitle);
            ImageView img1 = cardRecentAlert1.findViewById(R.id.imgAlert);

            if (savedAlerts != null && !savedAlerts.isEmpty()) {
                AlertModel alert1 = savedAlerts.get(0);
                if (txtTitle1 != null) txtTitle1.setText(alert1.getTitle() != null ? alert1.getTitle() : "Permission Alert");
                if (txtSub1 != null) txtSub1.setText((alert1.getAppName() != null ? alert1.getAppName() : "App") + " • " + (alert1.getTime() != null ? alert1.getTime() : "Today"));
                if (img1 != null) {
                    img1.setImageResource(R.drawable.ic_warning);
                    img1.setColorFilter("HIGH".equalsIgnoreCase(alert1.getCurrentRisk()) ? 0xFFFF3B30 : 0xFFFFC107);
                }
                cardRecentAlert1.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", alert1.getAppName());
                    intent.putExtra("PERMISSION", alert1.getPermissionChange());
                    intent.putExtra("RISK_LEVEL", alert1.getCurrentRisk());
                    intent.putExtra("DESCRIPTION", alert1.getExplanation());
                    startActivity(intent);
                });
            } else if (!highRisk.isEmpty()) {
                AppModel app1 = highRisk.get(0);
                if (txtTitle1 != null) txtTitle1.setText(app1.getApplicationName() + " - High Risk");
                if (txtSub1 != null) txtSub1.setText(app1.getApplicationName() + " • Camera & Location • Today");
                if (img1 != null) {
                    img1.setImageResource(R.drawable.ic_warning);
                    img1.setColorFilter(0xFFFF3B30);
                }
                cardRecentAlert1.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app1.getApplicationName());
                    intent.putExtra("PERMISSION", "Camera & Location Access");
                    intent.putExtra("RISK_LEVEL", "High");
                    intent.putExtra("DESCRIPTION", "This app has simultaneous access to multiple sensitive permissions.");
                    startActivity(intent);
                });
            } else if (!mediumRisk.isEmpty()) {
                AppModel app1 = mediumRisk.get(0);
                if (txtTitle1 != null) txtTitle1.setText(app1.getApplicationName() + " - Location/Storage");
                if (txtSub1 != null) txtSub1.setText(app1.getApplicationName() + " • Approximate Location • Today");
                if (img1 != null) {
                    img1.setImageResource(R.drawable.ic_warning);
                    img1.setColorFilter(0xFFFFC107);
                }
                cardRecentAlert1.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app1.getApplicationName());
                    intent.putExtra("PERMISSION", "Location & Storage Access");
                    intent.putExtra("RISK_LEVEL", "Medium");
                    intent.putExtra("DESCRIPTION", "Application requests location or storage access.");
                    startActivity(intent);
                });
            } else {
                if (txtTitle1 != null) txtTitle1.setText("All Applications Protected");
                if (txtSub1 != null) txtSub1.setText("Zero high-risk permissions detected • Today");
                if (img1 != null) {
                    img1.setImageResource(R.drawable.ic_shield);
                    img1.setColorFilter(0xFF00E676);
                }
                cardRecentAlert1.setOnClickListener(v -> {
                    if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_apps);
                });
            }
        }

        // Card 2
        if (cardRecentAlert2 != null) {
            TextView txtTitle2 = cardRecentAlert2.findViewById(R.id.txtAlertTitle);
            TextView txtSub2 = cardRecentAlert2.findViewById(R.id.txtAlertSubtitle);
            ImageView img2 = cardRecentAlert2.findViewById(R.id.imgAlert);

            if (savedAlerts != null && savedAlerts.size() > 1) {
                AlertModel alert2 = savedAlerts.get(1);
                if (txtTitle2 != null) txtTitle2.setText(alert2.getTitle() != null ? alert2.getTitle() : "Permission Alert");
                if (txtSub2 != null) txtSub2.setText((alert2.getAppName() != null ? alert2.getAppName() : "App") + " • " + (alert2.getTime() != null ? alert2.getTime() : "Today"));
                if (img2 != null) {
                    img2.setImageResource(R.drawable.ic_warning);
                    img2.setColorFilter("HIGH".equalsIgnoreCase(alert2.getCurrentRisk()) ? 0xFFFF3B30 : 0xFFFFC107);
                }
                cardRecentAlert2.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", alert2.getAppName());
                    intent.putExtra("PERMISSION", alert2.getPermissionChange());
                    intent.putExtra("RISK_LEVEL", alert2.getCurrentRisk());
                    intent.putExtra("DESCRIPTION", alert2.getExplanation());
                    startActivity(intent);
                });
            } else if (highRisk.size() > 1) {
                AppModel app2 = highRisk.get(1);
                if (txtTitle2 != null) txtTitle2.setText(app2.getApplicationName() + " - Sensitive Access");
                if (txtSub2 != null) txtSub2.setText(app2.getApplicationName() + " • Microphone & Storage • Today");
                if (img2 != null) {
                    img2.setImageResource(R.drawable.ic_warning);
                    img2.setColorFilter(0xFFFF3B30);
                }
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
                if (img2 != null) {
                    img2.setImageResource(R.drawable.ic_warning);
                    img2.setColorFilter(0xFFFFC107);
                }
                cardRecentAlert2.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AlertDetailsActivity.class);
                    intent.putExtra("APP_NAME", app2.getApplicationName());
                    intent.putExtra("PERMISSION", "Approximate Location");
                    intent.putExtra("RISK_LEVEL", "Medium");
                    intent.putExtra("DESCRIPTION", "Application accesses device location coordinates.");
                    startActivity(intent);
                });
            } else {
                if (txtTitle2 != null) txtTitle2.setText("Live Privacy Guard Active");
                if (txtSub2 != null) txtSub2.setText("Real-time background sensor monitor • Today");
                if (img2 != null) {
                    img2.setImageResource(R.drawable.ic_shield);
                    img2.setColorFilter(0xFF00E676);
                }
                cardRecentAlert2.setOnClickListener(v -> {
                    if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_monitor);
                });
            }
        }
    }

    private void setupPrivacyTimeline(int currentScore, String currentLevel, int totalApps) {
        // Timeline Item 1: Real-time Device Audit Scan
        if (cardTimelineItem1 != null) {
            TextView txtDate = cardTimelineItem1.findViewById(R.id.txtTimelineDate);
            TextView txtScore = cardTimelineItem1.findViewById(R.id.txtTimelineScore);
            TextView txtBadge = cardTimelineItem1.findViewById(R.id.txtRiskBadge);

            if (txtDate != null) txtDate.setText("Device Security Audit • Today");
            if (txtScore != null) txtScore.setText("Privacy Score : " + currentScore + " / 100");
            if (txtBadge != null) {
                if ("HIGH RISK".equalsIgnoreCase(currentLevel) || currentScore < 40) {
                    setRiskBadgeStyle(txtBadge, "HIGH", 0xFFFF3B30);
                } else if ("MEDIUM RISK".equalsIgnoreCase(currentLevel) || currentScore < 70) {
                    setRiskBadgeStyle(txtBadge, "MEDIUM", 0xFFFFC107);
                } else {
                    setRiskBadgeStyle(txtBadge, "LOW", 0xFF00E676);
                }
            }

            cardTimelineItem1.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, PrivacyReportActivity.class);
                startActivity(intent);
            });
        }

        // Timeline Item 2: Latest Recorded Privacy Event or Baseline Scan
        if (cardTimelineItem2 != null) {
            TextView txtDate = cardTimelineItem2.findViewById(R.id.txtTimelineDate);
            TextView txtScore = cardTimelineItem2.findViewById(R.id.txtTimelineScore);
            TextView txtBadge = cardTimelineItem2.findViewById(R.id.txtRiskBadge);

            List<AlertModel> alerts = sessionManager != null ? sessionManager.getPrivacyAlerts() : null;
            List<WebsiteModel> websites = sessionManager != null ? sessionManager.getWebsiteScans() : null;

            if (alerts != null && !alerts.isEmpty()) {
                AlertModel latestAlert = alerts.get(0);
                if (txtDate != null) txtDate.setText(latestAlert.getTime() != null ? latestAlert.getTime() : "Recent Event");
                if (txtScore != null) txtScore.setText(latestAlert.getTitle() != null ? latestAlert.getTitle() : "Permission Modified");
                if (txtBadge != null) {
                    String r = latestAlert.getCurrentRisk() != null ? latestAlert.getCurrentRisk().toUpperCase() : "ALERT";
                    if (r.contains("HIGH")) setRiskBadgeStyle(txtBadge, "HIGH", 0xFFFF3B30);
                    else if (r.contains("MED")) setRiskBadgeStyle(txtBadge, "MEDIUM", 0xFFFFC107);
                    else setRiskBadgeStyle(txtBadge, "LOW", 0xFF00E676);
                }
            } else if (websites != null && !websites.isEmpty()) {
                WebsiteModel latestWeb = websites.get(0);
                if (txtDate != null) txtDate.setText(latestWeb.getUrl() != null ? latestWeb.getUrl() : "Website Scan");
                if (txtScore != null) txtScore.setText("Safety Score : " + latestWeb.getRiskScore() + " / 100");
                if (txtBadge != null) {
                    String r = latestWeb.getRiskLevel() != null ? latestWeb.getRiskLevel().toUpperCase() : "SAFE";
                    if (r.contains("HIGH") || r.contains("UNSAFE")) setRiskBadgeStyle(txtBadge, "HIGH", 0xFFFF3B30);
                    else if (r.contains("MED")) setRiskBadgeStyle(txtBadge, "MEDIUM", 0xFFFFC107);
                    else setRiskBadgeStyle(txtBadge, "LOW", 0xFF00E676);
                }
            } else {
                if (txtDate != null) txtDate.setText("Baseline Application Audit");
                if (txtScore != null) txtScore.setText(totalApps + " Applications Scanned");
                if (txtBadge != null) {
                    setRiskBadgeStyle(txtBadge, "CLEAN", 0xFF00E676);
                }
            }

            cardTimelineItem2.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, PrivacyHistoryActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setRiskBadgeStyle(TextView txtBadge, String levelText, int color) {
        if (txtBadge == null) return;
        txtBadge.setText(levelText);
        android.graphics.drawable.GradientDrawable badgeBg = new android.graphics.drawable.GradientDrawable();
        badgeBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        badgeBg.setCornerRadius(20 * getResources().getDisplayMetrics().density);
        badgeBg.setColor(color);
        txtBadge.setBackground(badgeBg);
    }

    private void applyRiskScoreStyling(int score, String level) {
        if (txtRiskScore != null) {
            txtRiskScore.setText(String.valueOf(score));
        }

        int targetColor;
        if (score >= 70 || (level != null && level.contains("LOW"))) {
            targetColor = 0xFF00E676; // Vibrant Green
            if (txtRiskLevel != null) {
                txtRiskLevel.setText("LOW RISK\n(PROTECTED)");
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
                txtRiskLevel.setText("HIGH RISK\nDETECTED");
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