package com.example.securedroid.api;

import com.example.securedroid.models.AlertModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MonitoringApi {

    @GET("monitoring/events")
    Call<List<AlertModel>> getRecentEvents();

    @POST("monitoring/events")
    Call<AlertModel> recordEvent(@Body AlertModel alert);
}
