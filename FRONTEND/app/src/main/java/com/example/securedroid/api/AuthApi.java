package com.example.securedroid.api;

import com.example.securedroid.api.dto.LoginRequest;
import com.example.securedroid.api.dto.RegisterRequest;
import com.example.securedroid.api.dto.TokenResponse;
import com.example.securedroid.models.UserModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth/register")
    Call<UserModel> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @GET("auth/me")
    Call<UserModel> getMe();
}
