package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class RiskScoreModel {

    private int id;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("analysis_summary")
    private String analysisSummary;

    private List<RecommendationModel> recommendations = new ArrayList<>();
    private List<String> reasons = new ArrayList<>();

    public RiskScoreModel() {}

    public RiskScoreModel(float riskScore, String riskLevel, String analysisSummary) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.analysisSummary = analysisSummary;
    }

    public int getId() { return id; }
    public float getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getAnalysisSummary() { return analysisSummary; }
    public List<RecommendationModel> getRecommendations() { return recommendations; }
    public List<String> getReasons() { return reasons; }
}
