package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedroid.R;
import com.example.securedroid.adapters.TimelineAdapter;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.models.HistoryModel;
import com.example.securedroid.models.TimelineModel;
import com.example.securedroid.repository.DashboardRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivacyHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TimelineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_history);

        rvHistory = findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
        }

        loadHistoryData();
    }

    private void loadHistoryData() {
        ApiClient.getHistoryApi(this).getHistory().enqueue(new Callback<List<HistoryModel>>() {
            @Override
            public void onResponse(Call<List<HistoryModel>> call, Response<List<HistoryModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<TimelineModel> timelineList = new ArrayList<>();
                    for (HistoryModel h : response.body()) {
                        String title = h.getEventType() != null ? h.getEventType() : "Privacy Audit";
                        int score = (int) h.getRiskScore();
                        String level = score > 60 ? "HIGH" : (score > 30 ? "MEDIUM" : "LOW");
                        timelineList.add(new TimelineModel(title, score, level));
                    }
                    adapter = new TimelineAdapter(timelineList);
                    if (rvHistory != null) rvHistory.setAdapter(adapter);
                } else {
                    loadFallback();
                }
            }

            @Override
            public void onFailure(Call<List<HistoryModel>> call, Throwable t) {
                loadFallback();
            }
        });
    }

    private void loadFallback() {
        DashboardRepository repo = new DashboardRepository();
        adapter = new TimelineAdapter(repo.getPrivacyTimeline());
        if (rvHistory != null) rvHistory.setAdapter(adapter);
    }
}