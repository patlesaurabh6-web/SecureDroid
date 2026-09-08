package com.example.securedroid.api;

import com.example.securedroid.api.dto.DashboardSummaryResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DashboardApi {

    @GET("dashboard")
    Call<DashboardSummaryResponse> getDashboardSummary();
}
