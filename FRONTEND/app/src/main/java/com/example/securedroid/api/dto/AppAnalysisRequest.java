package com.example.securedroid.api.dto;

import com.google.gson.annotations.SerializedName;
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

    public AppAnalysisRequest(String packageName, String applicationName, String versionName, String developer, List<String> permissions) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.versionName = versionName;
        this.developer = developer;
        this.permissions = permissions;
    }

    public String getPackageName() { return packageName; }
    public String getApplicationName() { return applicationName; }
    public String getVersionName() { return versionName; }
    public String getDeveloper() { return developer; }
    public List<String> getPermissions() { return permissions; }
}
