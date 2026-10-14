package com.example.securedroid.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class AppAnalysisRequest {
    @SerializedName("package_name")
    private String packageName;

    @SerializedName("application_name")
    private String applicationName;

    @SerializedName("version_name")
    private String versionName;

    private String developer;
    private List<String> permissions;

    @SerializedName("declared_permissions")
    private List<String> declaredPermissions;

    @SerializedName("granted_permissions")
    private List<String> grantedPermissions;

    @SerializedName("denied_permissions")
    private List<String> deniedPermissions;

    @SerializedName("normal_permissions")
    private List<String> normalPermissions;

    public AppAnalysisRequest(String packageName, String applicationName, String versionName, String developer, List<String> permissions) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.versionName = versionName;
        this.developer = developer;
        this.permissions = permissions;
        this.grantedPermissions = permissions;
    }

    public AppAnalysisRequest(String packageName, String applicationName, List<String> permissions) {
        this(packageName, applicationName, "1.0", "Unknown", permissions);
    }

    public AppAnalysisRequest(String packageName, String applicationName, String versionName, String developer,
                              List<String> declaredPermissions, List<String> grantedPermissions,
                              List<String> deniedPermissions, List<String> normalPermissions) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.versionName = versionName;
        this.developer = developer;
        this.declaredPermissions = declaredPermissions != null ? declaredPermissions : new ArrayList<>();
        this.grantedPermissions = grantedPermissions != null ? grantedPermissions : new ArrayList<>();
        this.deniedPermissions = deniedPermissions != null ? deniedPermissions : new ArrayList<>();
        this.normalPermissions = normalPermissions != null ? normalPermissions : new ArrayList<>();
        this.permissions = new ArrayList<>(this.grantedPermissions);
    }

    public static AppAnalysisRequest fromAppModel(com.example.securedroid.models.AppModel app) {
        if (app == null) return null;
        return new AppAnalysisRequest(
                app.getPackageName(),
                app.getApplicationName(),
                app.getVersionName() != null ? app.getVersionName() : "1.0",
                app.getDeveloper() != null ? app.getDeveloper() : "Unknown",
                app.getDeclaredPermissions(),
                app.getGrantedSensitivePermissions(),
                app.getDeniedSensitivePermissions(),
                app.getNormalPermissions()
        );
    }

    public String getPackageName() { return packageName; }
    public String getApplicationName() { return applicationName; }
    public String getVersionName() { return versionName; }
    public String getDeveloper() { return developer; }
    public List<String> getPermissions() { return permissions; }
    public List<String> getDeclaredPermissions() { return declaredPermissions; }
    public List<String> getGrantedPermissions() { return grantedPermissions; }
    public List<String> getDeniedPermissions() { return deniedPermissions; }
    public List<String> getNormalPermissions() { return normalPermissions; }
}
