package com.example.securedroid.models;

public class RecommendationModel {

    private String title;
    private String recommendation;
    private String severity;

    public RecommendationModel() {
    }

    public RecommendationModel(String title, String recommendation, String severity) {
        this.title = title;
        this.recommendation = recommendation;
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}