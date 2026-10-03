package com.example.securedroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.notification.NotificationHelper;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PrivacyMonitorService extends Service {

    private static final String TAG = "PrivacyMonitorService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "PrivacyMonitorService started.");
        performPermissionAudit();
        return START_STICKY;
    }

    private void performPermissionAudit() {
        new Thread(() -> {
            try {
                SessionManager sessionManager = new SessionManager(getApplicationContext());
                List<AppModel> installedApps = PackageManagerHelper.getInstalledApps(getApplicationContext());

                String currentTime = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());

                for (AppModel app : installedApps) {
                    if (app == null || app.getPackageName() == null) continue;

                    String pkg = app.getPackageName();
                    List<String> currentPerms = app.getPermissions() != null ? app.getPermissions() : new ArrayList<>();
                    List<String> oldPerms = sessionManager.getAppPermissionsSnapshot(pkg);

                    if (oldPerms == null) {
                        // First snapshot baseline
                        sessionManager.saveAppPermissionsSnapshot(pkg, currentPerms);
                        continue;
                    }

                    Set<String> oldSet = new HashSet<>(oldPerms);
                    Set<String> newSet = new HashSet<>(currentPerms);

                    // P_added = P_new - P_old
                    Set<String> addedPerms = new HashSet<>(newSet);
                    addedPerms.removeAll(oldSet);

                    // P_removed = P_old - P_new
                    Set<String> removedPerms = new HashSet<>(oldSet);
                    removedPerms.removeAll(newSet);

                    if (!addedPerms.isEmpty() || !removedPerms.isEmpty()) {
                        StringBuilder changeSummary = new StringBuilder();
                        if (!addedPerms.isEmpty()) {
                            changeSummary.append("Added: ").append(formatPerms(addedPerms)).append(" ");
                        }
                        if (!removedPerms.isEmpty()) {
                            changeSummary.append("Removed: ").append(formatPerms(removedPerms));
                        }

                        // Query authoritative backend risk engine (calculate_privacy_risk)
                        try {
                            com.example.securedroid.api.dto.AppAnalysisRequest req = new com.example.securedroid.api.dto.AppAnalysisRequest(
                                    pkg,
                                    app.getApplicationName(),
                                    app.getVersionName(),
                                    app.getDeveloper(),
                                    currentPerms
                            );

                            retrofit2.Response<com.example.securedroid.models.RiskScoreModel> resp = com.example.securedroid.api.ApiClient
                                    .getAnalysisApi(getApplicationContext())
                                    .analyzeApplication(req)
                                    .execute();

                            String currRisk = "MONITORED";
                            if (resp.isSuccessful() && resp.body() != null) {
                                com.example.securedroid.models.RiskScoreModel model = resp.body();
                                currRisk = model.getRiskLevel() != null ? model.getRiskLevel().toUpperCase() : "MONITORED";
                                sessionManager.saveAppRiskScore(pkg, model);
                            }

                            String title = app.getApplicationName() + " - Permission Change";
                            String explanation = "Detected modification in declared permissions for " + app.getApplicationName() + ". Risk evaluated by backend engine as " + currRisk + ".";

                            AlertModel alert = new AlertModel(
                                    (int) (System.currentTimeMillis() % 100000),
                                    title,
                                    changeSummary.toString().trim(),
                                    currRisk,
                                    currentTime,
                                    app.getApplicationName(),
                                    app.getPackageName(),
                                    "Permission Change Detected",
                                    "PREVIOUS",
                                    currRisk,
                                    changeSummary.toString().trim(),
                                    explanation
                            );

                            sessionManager.addPrivacyAlert(alert);

                            NotificationHelper.showPrivacyAlert(
                                    getApplicationContext(),
                                    "Privacy Alert: " + app.getApplicationName(),
                                    "Permission Change: " + changeSummary + " | Risk: " + currRisk
                            );

                            // Update stored snapshot
                            sessionManager.saveAppPermissionsSnapshot(pkg, currentPerms);
                        } catch (Exception e) {
                            Log.e(TAG, "Error evaluating backend risk for permission change: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in permission audit: " + e.getMessage(), e);
            }
        }).start();
    }

    private String formatPerms(Set<String> perms) {
        List<String> readable = new ArrayList<>();
        for (String p : perms) {
            int lastDot = p.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < p.length() - 1) {
                readable.add(p.substring(lastDot + 1));
            } else {
                readable.add(p);
            }
        }
        return readable.toString();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
