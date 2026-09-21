package com.example.securedroid.api;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Current Active Laptop Wi-Fi IP (Update here if laptop Wi-Fi IP changes)
    private static String BASE_URL = "http://10.109.220.167:8000/api/";

    private static Retrofit retrofit = null;

    public static void setServerIp(String ipAddress) {
        BASE_URL = "http://" + ipAddress + ":8000/api/";
        retrofit = null;
    }

    public static synchronized Retrofit getClient(Context context) {
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
