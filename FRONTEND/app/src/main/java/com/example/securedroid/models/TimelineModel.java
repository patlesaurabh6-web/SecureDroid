package com.example.securedroid.models;

public class TimelineModel {

    private String date;
    private int privacyScore;
    private String riskLevel;

    public TimelineModel() {
    }

    public TimelineModel(String date, int privacyScore, String riskLevel) {
        this.date = date;
        this.privacyScore = privacyScore;
        this.riskLevel = riskLevel;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPrivacyScore() {
        return privacyScore;
    }

    public void setPrivacyScore(int privacyScore) {
        this.privacyScore = privacyScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}