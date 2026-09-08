package com.example.securedroid.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PolicyAnalysisResponse {
    @SerializedName("website_url")
    private String websiteUrl;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("risk_level")
    private String riskLevel;

    private String summary;

    @SerializedName("data_collection")
    private List<String> dataCollection;

    @SerializedName("third_party_sharing")
    private List<String> thirdPartySharing;

    @SerializedName("security_concerns")
    private List<String> securityConcerns;

    private List<String> recommendations;

    public String getWebsiteUrl() { return websiteUrl; }
    public float getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getSummary() { return summary; }
    public List<String> getDataCollection() { return dataCollection; }
    public List<String> getThirdPartySharing() { return thirdPartySharing; }
    public List<String> getSecurityConcerns() { return securityConcerns; }
    public List<String> getRecommendations() { return recommendations; }
}
