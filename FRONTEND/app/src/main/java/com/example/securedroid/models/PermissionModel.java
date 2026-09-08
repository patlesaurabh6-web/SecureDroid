package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;

public class PermissionModel {

    private int id;

    @SerializedName("permission_name")
    private String permissionName;

    @SerializedName("permission_category")
    private String permissionCategory;

    private String description;

    @SerializedName("risk_level")
    private String riskLevel;

    private boolean sensitive;
    private boolean granted = true;

    public PermissionModel() {}

    public PermissionModel(String permissionName, String permissionCategory, String description, String riskLevel, boolean sensitive) {
        this.permissionName = permissionName;
        this.permissionCategory = permissionCategory;
        this.description = description;
        this.riskLevel = riskLevel;
        this.sensitive = sensitive;
    }

    public int getId() { return id; }
    public String getPermissionName() { return permissionName; }
    public String getPermissionCategory() { return permissionCategory; }
    public String getDescription() { return description; }
    public String getRiskLevel() { return riskLevel; }
    public boolean isSensitive() { return sensitive; }
    public boolean isGranted() { return granted; }

    public void setGranted(boolean granted) { this.granted = granted; }
}
