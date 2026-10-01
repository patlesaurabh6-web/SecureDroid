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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppsFragment extends Fragment {

    public static final String ARG_FILTER = "arg_filter";

    private RecyclerView rvApps;
    private TextView txtHeader;
    private AppAdapter adapter;
    private String currentFilter = "ALL";

    public static AppsFragment newInstance(String filter) {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    public AppsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_FILTER)) {
            currentFilter = getArguments().getString(ARG_FILTER, "ALL");
        }
    }

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

    public static String getAppRiskCategory(AppModel app) {
        boolean hasCamera = false;
        boolean hasMic = false;
        boolean hasLocation = false;
        boolean hasSms = false;

        if (app.getPermissions() != null) {
            for (String p : app.getPermissions()) {
                String pUpper = p.toUpperCase();
                if (pUpper.contains("CAMERA")) hasCamera = true;
                if (pUpper.contains("RECORD_AUDIO") || pUpper.contains("MICROPHONE")) hasMic = true;
                if (pUpper.contains("LOCATION")) hasLocation = true;
                if (pUpper.contains("SMS") || pUpper.contains("CONTACT")) hasSms = true;
            }
        }

        if ((hasCamera && hasLocation) || (hasCamera && hasMic) || (hasSms && hasLocation)) {
            return "HIGH";
        } else if (hasCamera || hasMic || hasLocation || hasSms) {
            return "MEDIUM";
        } else {
            return "SAFE";
        }
    }

    private void scanInstalledApps() {
        if (getContext() == null) return;
        List<AppModel> allInstalledApps = PackageManagerHelper.getInstalledApps(getContext());

        List<AppModel> filteredList = new ArrayList<>();
        for (AppModel app : allInstalledApps) {
            String category = getAppRiskCategory(app);
            if ("ALL".equalsIgnoreCase(currentFilter)) {
                filteredList.add(app);
            } else if ("SAFE".equalsIgnoreCase(currentFilter) || "LOW".equalsIgnoreCase(currentFilter)) {
                if ("SAFE".equals(category)) filteredList.add(app);
            } else if ("MEDIUM".equalsIgnoreCase(currentFilter)) {
                if ("MEDIUM".equals(category)) filteredList.add(app);
            } else if ("HIGH".equalsIgnoreCase(currentFilter)) {
                if ("HIGH".equals(category)) filteredList.add(app);
            }
        }

        if (txtHeader != null) {
            if ("SAFE".equalsIgnoreCase(currentFilter) || "LOW".equalsIgnoreCase(currentFilter)) {
                txtHeader.setText("Safe / Low Risk Applications (" + filteredList.size() + ")");
            } else if ("MEDIUM".equalsIgnoreCase(currentFilter)) {
                txtHeader.setText("Medium Risk Applications (" + filteredList.size() + ")");
            } else if ("HIGH".equalsIgnoreCase(currentFilter)) {
                txtHeader.setText("High Risk Applications (" + filteredList.size() + ")");
            } else {
                txtHeader.setText("Scanned Installed Applications (" + filteredList.size() + ")");
            }
        }

        if (rvApps != null) {
            adapter = new AppAdapter(getContext(), filteredList);
            rvApps.setAdapter(adapter);
        }

        if (!filteredList.isEmpty()) {
            AppModel sampleApp = filteredList.get(0);
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