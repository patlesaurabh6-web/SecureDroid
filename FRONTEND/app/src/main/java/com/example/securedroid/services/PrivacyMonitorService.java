package com.example.securedroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.models.AlertModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivacyMonitorService extends Service {

    private static final String TAG = "PrivacyMonitorService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "PrivacyMonitorService started.");
        
        // Report continuous monitoring check to backend
        AlertModel event = new AlertModel(
                0,
                "Background Privacy Audit Active",
                "SecureDroid is actively checking installed permissions and security status.",
                "INFO",
                "Just now"
        );

        ApiClient.getMonitoringApi(this).recordEvent(event).enqueue(new Callback<AlertModel>() {
            @Override
            public void onResponse(Call<AlertModel> call, Response<AlertModel> response) {
                Log.d(TAG, "Privacy monitor event recorded: " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<AlertModel> call, Throwable t) {
                Log.e(TAG, "Privacy monitor event failed: " + t.getMessage());
            }
        });

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
