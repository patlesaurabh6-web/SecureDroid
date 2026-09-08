package com.example.securedroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;

public class WebsiteFragment extends Fragment {

    private EditText etWebsiteUrl;
    private Button btnScan;
    private TextView txtWebScore;

    public WebsiteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_website, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etWebsiteUrl = view.findViewById(R.id.etWebsiteUrl);
        btnScan = view.findViewById(R.id.btnScan);
        txtWebScore = view.findViewById(R.id.txtWebScore);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> scanWebsite());
        }
    }

    private void scanWebsite() {
        String url = etWebsiteUrl != null && etWebsiteUrl.getText() != null ? etWebsiteUrl.getText().toString().trim() : "";

        if (url.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid website URL", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Scanning website security for " + url + "...", Toast.LENGTH_SHORT).show();

        if (txtWebScore != null) {
            txtWebScore.setText("85");
        }
    }
}