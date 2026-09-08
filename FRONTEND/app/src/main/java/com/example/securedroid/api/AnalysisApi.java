package com.example.securedroid.api;

import com.example.securedroid.api.dto.AppAnalysisRequest;
import com.example.securedroid.api.dto.PolicyAnalysisRequest;
import com.example.securedroid.api.dto.PolicyAnalysisResponse;
import com.example.securedroid.models.RiskScoreModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AnalysisApi {

    @POST("analysis/application")
    Call<RiskScoreModel> analyzeApplication(@Body AppAnalysisRequest request);

    @POST("analysis/privacy-policy")
    Call<PolicyAnalysisResponse> analyzePrivacyPolicy(@Body PolicyAnalysisRequest request);
}
