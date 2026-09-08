package com.example.securedroid.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.example.securedroid.models.AppModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackageManagerHelper {

    public static List<AppModel> getInstalledApps(Context context) {
        List<AppModel> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        try {
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

            for (PackageInfo packageInfo : packages) {
                // Filter out system apps if desired, or include user installed apps
                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                if (!isSystemApp) {
                    String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                    String packageName = packageInfo.packageName;
                    String versionName = packageInfo.versionName != null ? packageInfo.versionName : "1.0.0";
                    String developer = packageInfo.applicationInfo.processName;

                    List<String> permissions = new ArrayList<>();
                    if (packageInfo.requestedPermissions != null) {
                        permissions.addAll(Arrays.asList(packageInfo.requestedPermissions));
                    }

                    AppModel app = new AppModel(packageName, appName, versionName, developer, permissions);
                    appList.add(app);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appList;
    }
}
