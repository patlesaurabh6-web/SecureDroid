package com.example.securedroid.repository;

import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.RecommendationModel;
import com.example.securedroid.models.StatisticModel;
import com.example.securedroid.models.TimelineModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {

    // ==========================================
    // Statistics
    // ==========================================

    public StatisticModel getStatistics() {
        return new StatisticModel(0, 0, 0, 0);
    }


    // ==========================================
    // Recent Alerts
    // ==========================================

    public List<AlertModel> getRecentAlerts() {
        return new ArrayList<>();
    }


    // ==========================================
    // Privacy Timeline
    // ==========================================

    public List<TimelineModel> getPrivacyTimeline() {
        return new ArrayList<>();
    }


    // ==========================================
    // AI Recommendation
    // ==========================================

    public RecommendationModel getRecommendation() {
        return new RecommendationModel(
                "Privacy Recommendation",
                "Review applications that frequently request sensitive permissions such as camera, microphone and location.",
                "MEDIUM"
        );
    }
}