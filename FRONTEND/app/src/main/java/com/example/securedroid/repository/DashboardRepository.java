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

        return new StatisticModel(
                45,
                12,
                3,
                41
        );
    }


    // ==========================================
    // Recent Alerts
    // ==========================================

    public List<AlertModel> getRecentAlerts() {

        List<AlertModel> alerts = new ArrayList<>();

        alerts.add(
                new AlertModel(
                        1,
                        "Camera Permission Detected",
                        "An application requested camera access",
                        "HIGH",
                        "10 minutes ago"
                )
        );

        alerts.add(
                new AlertModel(
                        2,
                        "Location Permission Detected",
                        "Location access was requested",
                        "MEDIUM",
                        "35 minutes ago"
                )
        );

        alerts.add(
                new AlertModel(
                        3,
                        "Contacts Permission",
                        "An application requested contacts access",
                        "LOW",
                        "1 hour ago"
                )
        );

        return alerts;
    }


    // ==========================================
    // Privacy Timeline
    // ==========================================

    public List<TimelineModel> getPrivacyTimeline() {

        List<TimelineModel> timeline = new ArrayList<>();

        timeline.add(
                new TimelineModel(
                        "Today",
                        82,
                        "LOW"
                )
        );

        timeline.add(
                new TimelineModel(
                        "Yesterday",
                        76,
                        "MEDIUM"
                )
        );

        timeline.add(
                new TimelineModel(
                        "2 Days Ago",
                        91,
                        "LOW"
                )
        );

        timeline.add(
                new TimelineModel(
                        "3 Days Ago",
                        64,
                        "HIGH"
                )
        );

        return timeline;
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