package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class AppModel {

    @SerializedName("id")
    private int id;

    @SerializedName("package_name")
    private String packageName;

    @SerializedName("application_name")
    private String applicationName;

    @SerializedName("version_name")
    private String versionName;

    @SerializedName("version_code")
    private int versionCode;

    private String developer;

    @SerializedName("analysis_status")
    private String analysisStatus;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("risk_level")
    private String riskLevel;

    private List<String> permissions = new ArrayList<>();

    public AppModel() {}

    public AppModel(String packageName, String applicationName, String versionName, String developer, List<String> permissions) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.versionName = versionName;
        this.developer = developer;
        this.permissions = permissions;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public int getVersionCode() { return versionCode; }
    public void setVersionCode(int versionCode) { this.versionCode = versionCode; }

    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }

    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }

    public float getRiskScore() { return riskScore; }
    public void setRiskScore(float riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel != null ? riskLevel : "LOW"; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
