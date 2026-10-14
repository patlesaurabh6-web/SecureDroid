package com.example.securedroid.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import com.example.securedroid.models.AppModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PackageManagerHelper {

    public static class AppPermissionBreakdown {
        public List<String> declaredPermissions = new ArrayList<>();
        public List<String> grantedSensitivePermissions = new ArrayList<>();
        public List<String> deniedSensitivePermissions = new ArrayList<>();
        public List<String> normalPermissions = new ArrayList<>();
    }

    public static boolean isPermissionGranted(PackageManager pm, PackageInfo packageInfo, int index, String permission, String packageName) {
        if (packageInfo != null && packageInfo.requestedPermissionsFlags != null && index >= 0 && index < packageInfo.requestedPermissionsFlags.length) {
            return (packageInfo.requestedPermissionsFlags[index] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
        }
        return false;
    }

    public static boolean isSensitiveOrDangerousPermission(PackageManager pm, String permission) {
        if (permission == null) return false;
        String u = permission.toUpperCase(Locale.ROOT);

        // 1. Direct platform dangerous/sensitive keywords
        if (u.contains("CAMERA")
                || u.contains("RECORD_AUDIO") || u.contains("MICROPHONE")
                || u.contains("LOCATION")
                || u.contains("CONTACT")
                || u.contains("CALENDAR")
                || u.contains("SMS") || u.contains("MMS")
                || u.contains("CALL_LOG") || u.contains("CALL_PHONE") || u.contains("READ_PHONE_STATE") || u.contains("READ_PHONE_NUMBERS")
                || u.contains("STORAGE") || u.contains("MEDIA")
                || u.contains("NOTIFICATION")
                || u.contains("BODY_SENSORS") || u.contains("ACTIVITY_RECOGNITION")
                || u.contains("SYSTEM_ALERT_WINDOW") || u.contains("PACKAGE_USAGE_STATS")
                || u.contains("BLUETOOTH_SCAN") || u.contains("BLUETOOTH_CONNECT") || u.contains("BLUETOOTH_ADVERTISE")) {
            return true;
        }

        // 2. PackageManager PermissionInfo check
        if (pm != null) {
            try {
                PermissionInfo pi = pm.getPermissionInfo(permission, 0);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    if (pi.getProtection() == PermissionInfo.PROTECTION_DANGEROUS) {
                        return true;
                    }
                }
                if ((pi.protectionLevel & 0x0f) == PermissionInfo.PROTECTION_DANGEROUS) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    public static AppPermissionBreakdown classifyPermissions(PackageManager pm, PackageInfo packageInfo, String packageName) {
        AppPermissionBreakdown breakdown = new AppPermissionBreakdown();
        if (packageInfo == null || packageInfo.requestedPermissions == null) {
            return breakdown;
        }

        for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
            String perm = packageInfo.requestedPermissions[i];
            breakdown.declaredPermissions.add(perm);

            boolean isGranted = isPermissionGranted(pm, packageInfo, i, perm, packageName);
            boolean isSensitive = isSensitiveOrDangerousPermission(pm, perm);

            if (isSensitive) {
                if (isGranted) {
                    breakdown.grantedSensitivePermissions.add(perm);
                } else {
                    breakdown.deniedSensitivePermissions.add(perm);
                }
            } else {
                breakdown.normalPermissions.add(perm);
            }
        }
        return breakdown;
    }

    public static String getPermissionDisplayName(String permission) {
        if (permission == null) return "";
        String u = permission.toUpperCase(Locale.ROOT);
        if (u.contains("ACCESS_FINE_LOCATION")) return "Precise Location";
        if (u.contains("ACCESS_COARSE_LOCATION")) return "Approximate Location";
        if (u.contains("ACCESS_BACKGROUND_LOCATION")) return "Background Location";
        if (u.contains("RECORD_AUDIO")) return "Microphone";
        if (u.contains("CAMERA")) return "Camera";
        if (u.contains("READ_CONTACTS") || u.contains("WRITE_CONTACTS")) return "Contacts";
        if (u.contains("READ_CALENDAR") || u.contains("WRITE_CALENDAR")) return "Calendar";
        if (u.contains("READ_CALL_LOG") || u.contains("WRITE_CALL_LOG")) return "Call Logs";
        if (u.contains("READ_SMS") || u.contains("SEND_SMS") || u.contains("RECEIVE_SMS")) return "SMS";
        if (u.contains("READ_PHONE_STATE") || u.contains("CALL_PHONE")) return "Phone";
        if (u.contains("READ_EXTERNAL_STORAGE") || u.contains("WRITE_EXTERNAL_STORAGE") || u.contains("MANAGE_EXTERNAL_STORAGE")) return "Storage";
        if (u.contains("READ_MEDIA_IMAGES") || u.contains("READ_MEDIA_VIDEO") || u.contains("READ_MEDIA_AUDIO")) return "Media & Photos";
        if (u.contains("POST_NOTIFICATIONS")) return "Notifications";
        if (u.contains("BODY_SENSORS")) return "Body Sensors";
        if (u.contains("ACTIVITY_RECOGNITION")) return "Physical Activity";
        if (u.contains("BLUETOOTH_SCAN") || u.contains("BLUETOOTH_CONNECT")) return "Nearby Devices";

        String clean = permission.replace("android.permission.", "");
        clean = clean.replace("_", " ");
        String[] parts = clean.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(p.substring(0, 1).toUpperCase(Locale.ROOT));
            if (p.length() > 1) {
                sb.append(p.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    public static List<AppModel> getInstalledApps(Context context) {
        List<AppModel> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        try {
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            for (PackageInfo packageInfo : packages) {
                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                if (!isSystemApp) {
                    String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                    String packageName = packageInfo.packageName;
                    String versionName = packageInfo.versionName != null ? packageInfo.versionName : "1.0.0";
                    String developer = packageInfo.applicationInfo.processName;

                    AppPermissionBreakdown b = classifyPermissions(pm, packageInfo, packageName);

                    AppModel app = new AppModel(packageName, appName, versionName, developer,
                            b.declaredPermissions, b.grantedSensitivePermissions,
                            b.deniedSensitivePermissions, b.normalPermissions);
                    appList.add(app);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appList;
    }

    public static AppModel getAppDetails(Context context, String packageName) {
        if (context == null || packageName == null) return null;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            String versionName = packageInfo.versionName != null ? packageInfo.versionName : "1.0.0";
            String developer = packageInfo.applicationInfo.processName;

            AppPermissionBreakdown b = classifyPermissions(pm, packageInfo, packageName);

            return new AppModel(packageName, appName, versionName, developer,
                    b.declaredPermissions, b.grantedSensitivePermissions,
                    b.deniedSensitivePermissions, b.normalPermissions);
        } catch (Exception e) {
            return null;
        }
    }
}
