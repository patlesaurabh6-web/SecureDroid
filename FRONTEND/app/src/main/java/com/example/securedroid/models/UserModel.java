package com.example.securedroid.models;

public class UserModel {

    private int userId;
    private String fullName;
    private String email;
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