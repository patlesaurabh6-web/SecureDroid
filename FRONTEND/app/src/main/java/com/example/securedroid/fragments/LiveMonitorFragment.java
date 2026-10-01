package com.example.securedroid.fragments;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;
import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class LiveMonitorFragment extends Fragment {

    private SwitchMaterial switchLiveMonitor;
    private TextView txtProtectionStatus;
    private ImageView imgRadarGlow;
    private TextView txtAppsScanned;
    private TextView txtPrivacyEventsCount;
    private TextView txtHighRiskCount;

    private View cardMonitoredApp1, cardMonitoredApp2;
    private ImageView iconApp1, iconApp2;
    private TextView txtMonitoredAppName1, txtMonitoredAppStatus1, txtMonitoredAppBadge1;
    private TextView txtMonitoredAppName2, txtMonitoredAppStatus2, txtMonitoredAppBadge2;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_monitor, container, false);

        sessionManager = new SessionManager(requireContext());

        switchLiveMonitor = view.findViewById(R.id.switchLiveMonitor);
        txtProtectionStatus = view.findViewById(R.id.txtProtectionStatus);
        imgRadarGlow = view.findViewById(R.id.imgRadarGlow);

        txtAppsScanned = view.findViewById(R.id.txtAppsScanned);
        txtPrivacyEventsCount = view.findViewById(R.id.txtPrivacyEventsCount);
        txtHighRiskCount = view.findViewById(R.id.txtHighRiskCount);

        cardMonitoredApp1 = view.findViewById(R.id.cardMonitoredApp1);
        cardMonitoredApp2 = view.findViewById(R.id.cardMonitoredApp2);
        iconApp1 = view.findViewById(R.id.iconApp1);
        iconApp2 = view.findViewById(R.id.iconApp2);
        txtMonitoredAppName1 = view.findViewById(R.id.txtMonitoredAppName1);
        txtMonitoredAppStatus1 = view.findViewById(R.id.txtMonitoredAppStatus1);
        txtMonitoredAppBadge1 = view.findViewById(R.id.txtMonitoredAppBadge1);
        txtMonitoredAppName2 = view.findViewById(R.id.txtMonitoredAppName2);
        txtMonitoredAppStatus2 = view.findViewById(R.id.txtMonitoredAppStatus2);
        txtMonitoredAppBadge2 = view.findViewById(R.id.txtMonitoredAppBadge2);

        startRadarAnimation();
        loadRealMonitorMetrics();

        switchLiveMonitor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtProtectionStatus.setText("Real-time protection is ON");
                txtProtectionStatus.setTextColor(0xFF00E676);
                startRadarAnimation();
            } else {
                txtProtectionStatus.setText("Real-time protection is OFF");
                txtProtectionStatus.setTextColor(0xFFFF3B30);
                if (imgRadarGlow != null) {
                    imgRadarGlow.clearAnimation();
                }
            }
        });

        return view;
    }

    private void loadRealMonitorMetrics() {
        if (getContext() == null) return;

        List<AppModel> apps = PackageManagerHelper.getInstalledApps(getContext());
        int totalApps = apps.size();
        int highRiskApps = 0;

        for (AppModel app : apps) {
            String risk = AppsFragment.getAppRiskCategory(app);
            if ("HIGH".equals(risk)) {
                highRiskApps++;
            }
        }

        List<AlertModel> alerts = sessionManager != null ? sessionManager.getPrivacyAlerts() : null;
        int alertCount = alerts != null ? alerts.size() : 0;

        if (txtAppsScanned != null) txtAppsScanned.setText(String.valueOf(totalApps));
        if (txtPrivacyEventsCount != null) txtPrivacyEventsCount.setText(String.valueOf(alertCount));
        if (txtHighRiskCount != null) txtHighRiskCount.setText(String.valueOf(highRiskApps));

        PackageManager pm = getContext().getPackageManager();

        if (apps.size() > 0 && cardMonitoredApp1 != null) {
            AppModel app1 = apps.get(0);
            if (txtMonitoredAppName1 != null) txtMonitoredAppName1.setText(app1.getApplicationName());
            if (txtMonitoredAppStatus1 != null) txtMonitoredAppStatus1.setText("Periodic permission check active");
            if (iconApp1 != null) {
                try {
                    Drawable d = pm.getApplicationIcon(app1.getPackageName());
                    if (d != null) {
                        iconApp1.setImageDrawable(d);
                        iconApp1.setColorFilter(null);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (apps.size() > 1 && cardMonitoredApp2 != null) {
            AppModel app2 = apps.get(1);
            if (txtMonitoredAppName2 != null) txtMonitoredAppName2.setText(app2.getApplicationName());
            if (txtMonitoredAppStatus2 != null) txtMonitoredAppStatus2.setText("Periodic permission check active");
            if (iconApp2 != null) {
                try {
                    Drawable d = pm.getApplicationIcon(app2.getPackageName());
                    if (d != null) {
                        iconApp2.setImageDrawable(d);
                        iconApp2.setColorFilter(null);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void startRadarAnimation() {
        if (imgRadarGlow != null) {
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(4000);
            rotate.setRepeatCount(Animation.INFINITE);
            imgRadarGlow.startAnimation(rotate);
        }
    }
}
