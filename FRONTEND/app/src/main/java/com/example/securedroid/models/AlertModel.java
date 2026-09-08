package com.example.securedroid.models;

public class AlertModel {

    private int alertId;
    private String title;
    private String message;
    private String riskLevel;
    private String time;

    public AlertModel() {
    }

    public AlertModel(int alertId, String title, String message, String riskLevel, String time) {
        this.alertId = alertId;
        this.title = title;
        this.message = message;
        this.riskLevel = riskLevel;
        this.time = time;
    }

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