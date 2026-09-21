package com.example.securedroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedroid.R;
import com.example.securedroid.adapters.AppAdapter;
import com.example.securedroid.api.ApiClient;
import com.example.securedroid.api.dto.AppAnalysisRequest;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.models.RiskScoreModel;
import com.example.securedroid.utils.PackageManagerHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppsFragment extends Fragment {

    private RecyclerView rvApps;
    private TextView txtHeader;
    private AppAdapter adapter;

    public AppsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvApps = view.findViewById(R.id.rvApps);
        txtHeader = view.findViewById(R.id.txtHeader);

        if (rvApps != null) {
            rvApps.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        scanInstalledApps();
    }

    private void scanInstalledApps() {
        if (getContext() == null) return;
        List<AppModel> installedApps = PackageManagerHelper.getInstalledApps(getContext());

        if (txtHeader != null) {
            txtHeader.setText("Scanned Installed Applications (" + installedApps.size() + ")");
        }

        if (rvApps != null) {
            adapter = new AppAdapter(getContext(), installedApps);
            rvApps.setAdapter(adapter);
        }

        if (!installedApps.isEmpty()) {
            AppModel sampleApp = installedApps.get(0);
            AppAnalysisRequest req = new AppAnalysisRequest(
                    sampleApp.getPackageName(),
                    sampleApp.getApplicationName(),
                    sampleApp.getVersionName(),
                    sampleApp.getDeveloper(),
                    sampleApp.getPermissions()
            );

            ApiClient.getAnalysisApi(getContext()).analyzeApplication(req).enqueue(new Callback<RiskScoreModel>() {
                @Override
                public void onResponse(Call<RiskScoreModel> call, Response<RiskScoreModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RiskScoreModel risk = response.body();
                        sampleApp.setRiskScore(risk.getRiskScore());
                        sampleApp.setRiskLevel(risk.getRiskLevel());
                    }
                }

                @Override
                public void onFailure(Call<RiskScoreModel> call, Throwable t) {
                    // Fail gracefully
                }
            });
        }
    }
}