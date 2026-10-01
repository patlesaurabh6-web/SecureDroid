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
                        String prevRisk = computeRiskLevel(oldPerms);
                        String currRisk = computeRiskLevel(currentPerms);

                        StringBuilder changeSummary = new StringBuilder();
                        if (!addedPerms.isEmpty()) {
                            changeSummary.append("Added: ").append(formatPerms(addedPerms)).append(" ");
                        }
                        if (!removedPerms.isEmpty()) {
                            changeSummary.append("Removed: ").append(formatPerms(removedPerms));
                        }

                        String title = app.getApplicationName() + " - Permission Change";
                        String explanation = "Detected modification in declared permissions for " + app.getApplicationName() + ". Risk changed from " + prevRisk + " to " + currRisk + ".";

                        AlertModel alert = new AlertModel(
                                (int) (System.currentTimeMillis() % 100000),
                                title,
                                changeSummary.toString().trim(),
                                currRisk,
                                currentTime,
                                app.getApplicationName(),
                                app.getPackageName(),
                                "Permission Change Detected",
                                prevRisk,
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
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in permission audit: " + e.getMessage(), e);
            }
        }).start();
    }

    private String computeRiskLevel(List<String> perms) {
        if (perms == null) return "LOW";
        boolean hasCamera = false;
        boolean hasMic = false;
        boolean hasLocation = false;
        boolean hasSms = false;

        for (String p : perms) {
            String pUpper = p.toUpperCase();
            if (pUpper.contains("CAMERA")) hasCamera = true;
            if (pUpper.contains("RECORD_AUDIO") || pUpper.contains("MICROPHONE")) hasMic = true;
            if (pUpper.contains("LOCATION")) hasLocation = true;
            if (pUpper.contains("SMS") || pUpper.contains("CONTACT")) hasSms = true;
        }

        if ((hasCamera && hasLocation) || (hasCamera && hasMic) || (hasSms && hasLocation)) {
            return "HIGH";
        } else if (hasCamera || hasMic || hasLocation || hasSms) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
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
