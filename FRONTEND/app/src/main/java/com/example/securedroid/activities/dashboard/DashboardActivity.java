package com.example.securedroid.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting, txtUserName;
    private TextView txtRiskScore, txtRiskLevel;
    private MaterialButton btnAnalyze;
    private BottomNavigationView bottomNavigation;
    private View dashboardScroll;
    private FrameLayout fragmentContainer;
    private View layoutNotifications;

    private View cardApps, cardWebsite, cardAlerts, cardSafeApps;
    private View cardInstalledApps, cardWebsiteAnalysis, cardHistory, cardAIAdvisor;

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

        // Notification Icon Click Listener
        if (layoutNotifications != null) {
            layoutNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, NotificationHistoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> {
                loadDashboardMetrics();
                Toast.makeText(this, "Scanning device applications & privacy status...", Toast.LENGTH_SHORT).show();
            });
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
        int score = (int) summary.getOverallRiskScore();
        String level = summary.getRiskLevel();
        applyRiskScoreStyling(score, level);

        if (cardApps != null) {
            ((TextView) cardApps.findViewById(R.id.txtValue)).setText(String.valueOf(summary.getTotalApplications()));
            ((TextView) cardApps.findViewById(R.id.txtTitle)).setText("Total Apps");
        }
        if (cardWebsite != null) {
            ((TextView) cardWebsite.findViewById(R.id.txtValue)).setText(String.valueOf(summary.getMediumRiskApplications()));
            ((TextView) cardWebsite.findViewById(R.id.txtTitle)).setText("Websites");
        }
        if (cardAlerts != null) {
            ((TextView) cardAlerts.findViewById(R.id.txtValue)).setText(String.valueOf(summary.getHighRiskApplications()));
            ((TextView) cardAlerts.findViewById(R.id.txtTitle)).setText("High Risk");
            ((TextView) cardAlerts.findViewById(R.id.txtValue)).setTextColor(0xFFFF3B30);
        }
        if (cardSafeApps != null) {
            ((TextView) cardSafeApps.findViewById(R.id.txtValue)).setText(String.valueOf(summary.getLowRiskApplications()));
            ((TextView) cardSafeApps.findViewById(R.id.txtTitle)).setText("Safe Apps");
            ((TextView) cardSafeApps.findViewById(R.id.txtValue)).setTextColor(0xFF00E676);
        }
    }

    private void calculateLiveDeviceMetrics() {
        List<AppModel> apps = PackageManagerHelper.getInstalledApps(this);
        int totalApps = apps.size();
        int highRiskApps = 0;
        int mediumRiskApps = 0;
        int safeApps = 0;

        for (AppModel app : apps) {
            boolean hasCamera = false;
            boolean hasMic = false;
            boolean hasLocation = false;

            if (app.getPermissions() != null) {
                for (String p : app.getPermissions()) {
                    if (p.contains("CAMERA")) hasCamera = true;
                    if (p.contains("RECORD_AUDIO")) hasMic = true;
                    if (p.contains("ACCESS_FINE_LOCATION") || p.contains("ACCESS_COARSE_LOCATION")) hasLocation = true;
                }
            }

            if (hasCamera && hasLocation) {
                highRiskApps++;
            } else if (hasCamera || hasMic || hasLocation) {
                mediumRiskApps++;
            } else {
                safeApps++;
            }
        }

        // Privacy Score calculation: Higher is safer
        int score = totalApps > 0 ? Math.max(15, 100 - (highRiskApps * 10) - (mediumRiskApps * 3)) : 85;
        String level = score >= 70 ? "LOW RISK" : (score >= 40 ? "MEDIUM RISK" : "HIGH RISK");

        applyRiskScoreStyling(score, level);

        // Update Stat Cards with real device counts
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
    }

    private void applyRiskScoreStyling(int score, String level) {
        if (txtRiskScore != null) {
            txtRiskScore.setText(String.valueOf(score));
        }

        if (txtRiskLevel != null) {
            txtRiskLevel.setText(level != null ? level.toUpperCase() : "SAFE");

            // Dynamic color coding: Green for Low Risk, Yellow for Medium Risk, Red for High Risk
            if (score >= 70 || (level != null && level.contains("LOW"))) {
                txtRiskScore.setTextColor(0xFF00E676); // Green
                txtRiskLevel.setTextColor(0xFF00E676); // Green
                txtRiskLevel.setText("LOW RISK (PROTECTED)");
            } else if (score >= 40 || (level != null && level.contains("MEDIUM"))) {
                txtRiskScore.setTextColor(0xFFFFC107); // Yellow/Orange
                txtRiskLevel.setTextColor(0xFFFFC107); // Yellow/Orange
                txtRiskLevel.setText("MEDIUM RISK");
            } else {
                txtRiskScore.setTextColor(0xFFFF3B30); // Red
                txtRiskLevel.setTextColor(0xFFFF3B30); // Red
                txtRiskLevel.setText("HIGH RISK DETECTED");
            }
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