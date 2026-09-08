package com.example.securedroid.api;

import com.example.securedroid.models.AppModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApplicationApi {

    @GET("applications")
    Call<List<AppModel>> getApplications();

    @GET("applications/{id}")
    Call<AppModel> getApplicationById(@Path("id") int id);
}
