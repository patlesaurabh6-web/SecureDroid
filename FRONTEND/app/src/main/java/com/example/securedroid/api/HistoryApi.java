package com.example.securedroid.api;

import com.example.securedroid.models.HistoryModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HistoryApi {

    @GET("history")
    Call<List<HistoryModel>> getHistory();
}
