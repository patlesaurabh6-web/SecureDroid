package com.example.securedroid.models;

public class AlertModel {

    private int alertId;
    private String title;
    private String message;
    private String riskLevel;
    private String time;
    private String appName;
    private String packageName;
    private String eventType;
    private String previousRisk;
    private String currentRisk;
    private String permissionChange;
    private String explanation;

    public AlertModel() {
    }

    public AlertModel(int alertId, String title, String message, String riskLevel, String time) {
        this.alertId = alertId;
        this.title = title;
        this.message = message;
        this.riskLevel = riskLevel;
        this.time = time;
    }

    public AlertModel(int alertId, String title, String message, String riskLevel, String time,
                      String appName, String packageName, String eventType,
                      String previousRisk, String currentRisk, String permissionChange, String explanation) {
        this.alertId = alertId;
        this.title = title;
        this.message = message;
        this.riskLevel = riskLevel;
        this.time = time;
        this.appName = appName;
        this.packageName = packageName;
        this.eventType = eventType;
        this.previousRisk = previousRisk;
        this.currentRisk = currentRisk;
        this.permissionChange = permissionChange;
        this.explanation = explanation;
    }

    public String getAppName() { return appName != null ? appName : title; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getEventType() { return eventType != null ? eventType : "Permission Change"; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPreviousRisk() { return previousRisk != null ? previousRisk : "LOW"; }
    public void setPreviousRisk(String previousRisk) { this.previousRisk = previousRisk; }

    public String getCurrentRisk() { return currentRisk != null ? currentRisk : riskLevel; }
    public void setCurrentRisk(String currentRisk) { this.currentRisk = currentRisk; }

    public String getPermissionChange() { return permissionChange != null ? permissionChange : title; }
    public void setPermissionChange(String permissionChange) { this.permissionChange = permissionChange; }

    public String getExplanation() { return explanation != null ? explanation : message; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}