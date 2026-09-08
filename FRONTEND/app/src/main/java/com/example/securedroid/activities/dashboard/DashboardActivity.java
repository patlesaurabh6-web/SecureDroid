package com.example.securedroid.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.DashboardSummaryResponse;
import com.example.securedroid.fragments.AppsFragment;
import com.example.securedroid.fragments.LiveMonitorFragment;
import com.example.securedroid.fragments.ProfileFragment;
import com.example.securedroid.fragments.WebsiteFragment;
import com.example.securedroid.repository.DashboardRepository;
import com.example.securedroid.services.PrivacyMonitorService;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

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

        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> {
                loadDashboardMetrics();
                Toast.makeText(this, "Scanning privacy status...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupGreeting() {
        if (txtGreeting != null) txtGreeting.setText("Hello, " + sessionManager.getUserName());
        if (txtUserName != null) txtUserName.setText("Security Dashboard");
    }

    private void loadDashboardMetrics() {
        ApiClient.getDashboardApi(this).getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardSummaryResponse summary = response.body();
                    updateUI(summary);
                } else {
                    loadFallbackRepositoryData();
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                loadFallbackRepositoryData();
            }
        });
    }

    private void updateUI(DashboardSummaryResponse summary) {
        if (txtRiskScore != null) {
            txtRiskScore.setText(String.valueOf((int) summary.getOverallRiskScore()));
        }
        if (txtRiskLevel != null) {
            txtRiskLevel.setText(summary.getRiskLevel() + " RISK");
        }
    }

    private void loadFallbackRepositoryData() {
        if (txtRiskScore != null) txtRiskScore.setText("82");
        if (txtRiskLevel != null) txtRiskLevel.setText("LOW RISK");
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