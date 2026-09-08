package com.example.securedroid.api.dto;

import com.google.gson.annotations.SerializedName;

public class PolicyAnalysisRequest {
    @SerializedName("privacy_policy_text")
    private String privacyPolicyText;

    @SerializedName("website_url")
    private String websiteUrl;

    public PolicyAnalysisRequest(String privacyPolicyText, String websiteUrl) {
        this.privacyPolicyText = privacyPolicyText;
        this.websiteUrl = websiteUrl;
    }

    public String getPrivacyPolicyText() { return privacyPolicyText; }
    public String getWebsiteUrl() { return websiteUrl; }
}
