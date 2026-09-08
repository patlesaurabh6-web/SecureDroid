package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;

public class HistoryModel {

    private int id;

    @SerializedName("application_name")
    private String applicationName;

    @SerializedName("event_type")
    private String eventType;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("created_at")
    private String createdAt;

    public HistoryModel() {}

    public HistoryModel(int id, String applicationName, String eventType, float riskScore, String createdAt) {
        this.id = id;
        this.applicationName = applicationName;
        this.eventType = eventType;
        this.riskScore = riskScore;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getApplicationName() { return applicationName; }
    public String getEventType() { return eventType; }
    public float getRiskScore() { return riskScore; }
    public String getCreatedAt() { return createdAt; }
}
