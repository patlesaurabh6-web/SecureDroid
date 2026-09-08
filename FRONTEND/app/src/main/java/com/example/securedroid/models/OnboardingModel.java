package com.example.securedroid.models;

public class OnboardingModel {

    private final int animationResId;
    private final String title;
    private final String description;

    public OnboardingModel(int animationResId, String title, String description) {
        this.animationResId = animationResId;
        this.title = title;
        this.description = description;
    }

    public int getAnimationResId() {
        return animationResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}