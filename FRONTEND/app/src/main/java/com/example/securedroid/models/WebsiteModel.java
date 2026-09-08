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

    public WebsiteModel() {}

    public WebsiteModel(String url, String domain, float riskScore, String riskLevel, String summary) {
        this.url = url;
        this.domain = domain;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.summary = summary;
    }

    public int getId() { return id; }
    public String getUrl() { return url; }
    public String getDomain() { return domain; }
    public float getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getSummary() { return summary; }
}
