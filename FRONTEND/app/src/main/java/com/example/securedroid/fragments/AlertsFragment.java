package com.example.securedroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedroid.R;
import com.example.securedroid.adapters.AlertAdapter;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.models.AlertModel;
import com.example.securedroid.repository.DashboardRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertsFragment extends Fragment {

    private RecyclerView rvAlerts;
    private AlertAdapter adapter;

    public AlertsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        if (rvAlerts != null) {
            rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        loadAlerts();
    }

    private void loadAlerts() {
        if (getContext() == null) return;

        ApiClient.getMonitoringApi(getContext()).getRecentEvents().enqueue(new Callback<List<AlertModel>>() {
            @Override
            public void onResponse(Call<List<AlertModel>> call, Response<List<AlertModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    adapter = new AlertAdapter(response.body());
                    if (rvAlerts != null) rvAlerts.setAdapter(adapter);
                } else {
                    loadFallback();
                }
            }

            @Override
            public void onFailure(Call<List<AlertModel>> call, Throwable t) {
                loadFallback();
            }
        });
    }

    private void loadFallback() {
        DashboardRepository repo = new DashboardRepository();
        adapter = new AlertAdapter(repo.getRecentAlerts());
        if (rvAlerts != null) rvAlerts.setAdapter(adapter);
    }
}