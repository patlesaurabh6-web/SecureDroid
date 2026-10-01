package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;

public class WebsiteModel {

    private int id;
    private String url;
    private String domain;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("risk_level")
    private String riskLevel;

    private String summary;

    private String dateTime;
    private String detectedIndicators;
    private String recommendation;

    public WebsiteModel() {}

    public WebsiteModel(String url, String domain, float riskScore, String riskLevel, String summary) {
        this.url = url;
        this.domain = domain;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.summary = summary;
    }

    public WebsiteModel(String url, String domain, float riskScore, String riskLevel, String summary, String dateTime, String detectedIndicators, String recommendation) {
        this.url = url;
        this.domain = domain;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.dateTime = dateTime;
        this.detectedIndicators = detectedIndicators;
        this.recommendation = recommendation;
    }

    public int getId() { return id; }
    public String getUrl() { return url; }
    public String getDomain() { return domain; }
    public float getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getSummary() { return summary; }
    public String getDateTime() { return dateTime; }
    public String getDetectedIndicators() { return detectedIndicators; }
    public String getRecommendation() { return recommendation; }

    public void setId(int id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setDomain(String domain) { this.domain = domain; }
    public void setRiskScore(float riskScore) { this.riskScore = riskScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public void setDetectedIndicators(String detectedIndicators) { this.detectedIndicators = detectedIndicators; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
