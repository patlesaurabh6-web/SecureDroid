package com.example.securedroid.models;

public class StatisticModel {

    private int apps;
    private int websites;
    private int alerts;
    private int safeApps;

    public StatisticModel() {
    }

    public StatisticModel(int apps, int websites, int alerts, int safeApps) {
        this.apps = apps;
        this.websites = websites;
        this.alerts = alerts;
        this.safeApps = safeApps;
    }

    public int getApps() {
        return apps;
    }

    public void setApps(int apps) {
        this.apps = apps;
    }

    public int getWebsites() {
        return websites;
    }

    public void setWebsites(int websites) {
        this.websites = websites;
    }

    public int getAlerts() {
        return alerts;
    }

    public void setAlerts(int alerts) {
        this.alerts = alerts;
    }

    public int getSafeApps() {
        return safeApps;
    }

    public void setSafeApps(int safeApps) {
        this.safeApps = safeApps;
    }
}