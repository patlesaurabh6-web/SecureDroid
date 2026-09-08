package com.example.securedroid.api.dto;

import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.StatisticModel;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardSummaryResponse {
    @SerializedName("overall_risk_score")
    private float overallRiskScore;

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("total_applications")
    private int totalApplications;

    @SerializedName("high_risk_applications")
    private int highRiskApplications;

    @SerializedName("medium_risk_applications")
    private int mediumRiskApplications;

    @SerializedName("low_risk_applications")
    private int lowRiskApplications;

    @SerializedName("recommendation_title")
    private String recommendationTitle;

    @SerializedName("recommendation_summary")
    private String recommendationSummary;

    public float getOverallRiskScore() { return overallRiskScore; }
    public String getRiskLevel() { return riskLevel; }
    public int getTotalApplications() { return totalApplications; }
    public int getHighRiskApplications() { return highRiskApplications; }
    public int getMediumRiskApplications() { return mediumRiskApplications; }
    public int getLowRiskApplications() { return lowRiskApplications; }
    public String getRecommendationTitle() { return recommendationTitle; }
    public String getRecommendationSummary() { return recommendationSummary; }
}
