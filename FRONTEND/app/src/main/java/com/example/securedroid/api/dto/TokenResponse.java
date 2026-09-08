package com.example.securedroid.api.dto;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("user_id")
    private int userId;

    private String email;
    private String name;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}
