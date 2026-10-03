package com.example.securedroid.api;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Default connection via USB ADB Reverse (127.0.0.1:8000) or Wi-Fi IP
    public static String BASE_URL = "http://127.0.0.1:8000/api/";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    private static Retrofit retrofit = null;

    public static void setServerIp(String ipAddress) {
        if (ipAddress.startsWith("http://") || ipAddress.startsWith("https://")) {
            BASE_URL = ipAddress.endsWith("/") ? ipAddress : ipAddress + "/";
        } else {
            BASE_URL = "http://" + ipAddress + ":8000/api/";
        }
        retrofit = null;
    }

    public static synchronized Retrofit getClient(Context context) {
        if (context != null) {
            String savedIp = com.example.securedroid.utils.SessionManager.getInstance(context).getServerIp();
            if (savedIp != null && !savedIp.trim().isEmpty()) {
                String targetUrl;
                if (savedIp.startsWith("http://") || savedIp.startsWith("https://")) {
                    targetUrl = savedIp.endsWith("/") ? savedIp : savedIp + "/";
                } else {
                    targetUrl = "http://" + savedIp.trim() + ":8000/api/";
                }
                if (!targetUrl.equals(BASE_URL)) {
                    BASE_URL = targetUrl;
                    retrofit = null;
                }
            }
        }

        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi(Context context) {
        return getClient(context).create(AuthApi.class);
    }

    public static DashboardApi getDashboardApi(Context context) {
        return getClient(context).create(DashboardApi.class);
    }

    public static ApplicationApi getApplicationApi(Context context) {
        return getClient(context).create(ApplicationApi.class);
    }

    public static AnalysisApi getAnalysisApi(Context context) {
        return getClient(context).create(AnalysisApi.class);
    }

    public static MonitoringApi getMonitoringApi(Context context) {
        return getClient(context).create(MonitoringApi.class);
    }

    public static HistoryApi getHistoryApi(Context context) {
        return getClient(context).create(HistoryApi.class);
    }
}
