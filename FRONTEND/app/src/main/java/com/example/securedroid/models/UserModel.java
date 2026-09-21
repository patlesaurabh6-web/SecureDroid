package com.example.securedroid.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("id")
    private int userId;

    @SerializedName("name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("profileImage")
    private String profileImage;

    public UserModel() {
    }

    public UserModel(int userId, String fullName, String email, String profileImage) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.profileImage = profileImage;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}