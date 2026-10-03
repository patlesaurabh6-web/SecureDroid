package com.example.securedroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "securedroid_session";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveAuthToken(String token, int userId, String name, String email) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getAuthToken() {
        return pref.getString(KEY_JWT_TOKEN, null);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false) && getAuthToken() != null;
    }

    public void saveUserProfile(String name, String email, String avatarUri) {
        if (name != null) editor.putString(KEY_USER_NAME, name);
        if (email != null) editor.putString(KEY_USER_EMAIL, email);
        if (avatarUri != null) editor.putString("user_avatar_uri", avatarUri);
        editor.apply();
    }

    public String getAvatarUri() {
        return pref.getString("user_avatar_uri", null);
    }

    public void saveLastScanTime(String time) {
        editor.putString("last_scan_time", time);
        editor.apply();
    }

    public String getLastScanTime() {
        return pref.getString("last_scan_time", null);
    }

    // Website Scans History Storage
    public void addWebsiteScan(com.example.securedroid.models.WebsiteModel scan) {
        java.util.List<com.example.securedroid.models.WebsiteModel> list = getWebsiteScans();
        list.add(0, scan);
        if (list.size() > 50) list = list.subList(0, 50);
        String json = new com.google.gson.Gson().toJson(list);
        editor.putString("website_scans_history", json);
        editor.apply();
    }

    public java.util.List<com.example.securedroid.models.WebsiteModel> getWebsiteScans() {
        String json = pref.getString("website_scans_history", null);
        if (json == null || json.isEmpty()) return new java.util.ArrayList<>();
        try {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<com.example.securedroid.models.WebsiteModel>>(){}.getType();
            return new com.google.gson.Gson().fromJson(json, type);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    // Privacy & Permission Alert Events Storage
    public void addPrivacyAlert(com.example.securedroid.models.AlertModel alert) {
        java.util.List<com.example.securedroid.models.AlertModel> list = getPrivacyAlerts();
        list.add(0, alert);
        if (list.size() > 100) list = list.subList(0, 100);
        String json = new com.google.gson.Gson().toJson(list);
        editor.putString("privacy_alerts_history", json);
        editor.apply();
    }

    public java.util.List<com.example.securedroid.models.AlertModel> getPrivacyAlerts() {
        String json = pref.getString("privacy_alerts_history", null);
        if (json == null || json.isEmpty()) return new java.util.ArrayList<>();
        try {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<com.example.securedroid.models.AlertModel>>(){}.getType();
            return new com.google.gson.Gson().fromJson(json, type);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    // App Permissions Snapshot (for P_old vs P_new comparison)
    public void saveAppPermissionsSnapshot(String packageName, java.util.List<String> perms) {
        String json = perms != null ? new com.google.gson.Gson().toJson(perms) : "[]";
        editor.putString("app_perms_snap_" + packageName, json);
        editor.apply();
    }

    public java.util.List<String> getAppPermissionsSnapshot(String packageName) {
        String json = pref.getString("app_perms_snap_" + packageName, null);
        if (json == null) return null; // null indicates never scanned before
        try {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<String>>(){}.getType();
            return new com.google.gson.Gson().fromJson(json, type);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    // App Risk Score caching (stores backend RiskScoreModel evaluated by calculate_privacy_risk)
    public void saveAppRiskScore(String packageName, com.example.securedroid.models.RiskScoreModel model) {
        if (packageName == null || model == null) return;
        String json = new com.google.gson.Gson().toJson(model);
        editor.putString("app_risk_model_" + packageName, json);
        editor.apply();
    }

    public com.example.securedroid.models.RiskScoreModel getAppRiskScore(String packageName) {
        if (packageName == null) return null;
        String json = pref.getString("app_risk_model_" + packageName, null);
        if (json == null) return null;
        try {
            return new com.google.gson.Gson().fromJson(json, com.example.securedroid.models.RiskScoreModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveServerIp(String ip) {
        editor.putString("server_ip", ip);
        editor.apply();
    }

    public String getServerIp() {
        return pref.getString("server_ip", null);
    }

    public void logout() {
        String savedIp = getServerIp();
        editor.clear();
        if (savedIp != null) {
            editor.putString("server_ip", savedIp);
        }
        editor.apply();
    }
}
