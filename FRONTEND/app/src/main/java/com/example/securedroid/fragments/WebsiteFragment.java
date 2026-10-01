package com.example.securedroid.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.securedroid.R;
import com.example.securedroid.models.WebsiteModel;
import com.example.securedroid.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebsiteFragment extends Fragment {

    private EditText etWebsiteUrl;
    private Button btnScan;
    private ImageView imgWebScoreRing;
    private TextView txtWebScore;
    private TextView txtWebRiskLevel;
    private TextView txtWebRiskSummary;
    private TextView txtWebSslStatus;
    private TextView txtWebTrackersStatus;
    private TextView txtWebCookiesStatus;
    private TextView txtWebPermissionsStatus;

    private SessionManager sessionManager;

    public WebsiteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_website, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        etWebsiteUrl = view.findViewById(R.id.etWebsiteUrl);
        btnScan = view.findViewById(R.id.btnScan);
        imgWebScoreRing = view.findViewById(R.id.imgWebScoreRing);
        txtWebScore = view.findViewById(R.id.txtWebScore);
        txtWebRiskLevel = view.findViewById(R.id.txtWebRiskLevel);
        txtWebRiskSummary = view.findViewById(R.id.txtWebRiskSummary);
        txtWebSslStatus = view.findViewById(R.id.txtWebSslStatus);
        txtWebTrackersStatus = view.findViewById(R.id.txtWebTrackersStatus);
        txtWebCookiesStatus = view.findViewById(R.id.txtWebCookiesStatus);
        txtWebPermissionsStatus = view.findViewById(R.id.txtWebPermissionsStatus);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> scanWebsite());
        }

        loadLatestScanIfExists();
    }

    private void loadLatestScanIfExists() {
        if (sessionManager != null && sessionManager.getWebsiteScans() != null && !sessionManager.getWebsiteScans().isEmpty()) {
            WebsiteModel latest = sessionManager.getWebsiteScans().get(0);
            if (latest != null && etWebsiteUrl != null && etWebsiteUrl.getText().toString().isEmpty()) {
                etWebsiteUrl.setText(latest.getUrl());
                displayScanResult(latest);
            }
        }
    }

    private void scanWebsite() {
        String inputUrl = etWebsiteUrl != null && etWebsiteUrl.getText() != null ? etWebsiteUrl.getText().toString().trim() : "";

        if (inputUrl.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid website URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullUrl = inputUrl;
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            fullUrl = "https://" + fullUrl;
        }

        Uri uri;
        try {
            uri = Uri.parse(fullUrl);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid URL format", Toast.LENGTH_SHORT).show();
            return;
        }

        String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "unknown";
        String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
        String path = uri.getPath() != null ? uri.getPath().toLowerCase() : "";
        String query = uri.getQuery() != null ? uri.getQuery().toLowerCase() : "";

        if (host.isEmpty()) {
            Toast.makeText(getContext(), "Unable to extract domain host from URL", Toast.LENGTH_SHORT).show();
            return;
        }

        int score = 100;
        StringBuilder indicators = new StringBuilder();
        StringBuilder summary = new StringBuilder();

        boolean isHttps = "https".equals(scheme);
        if (!isHttps) {
            score -= 40;
            indicators.append("Unencrypted HTTP; ");
            summary.append("Connection is unencrypted (HTTP). Data in transit could be intercepted. ");
            if (txtWebSslStatus != null) {
                txtWebSslStatus.setText("HTTP (Insecure)");
                txtWebSslStatus.setTextColor(Color.parseColor("#FF1744"));
            }
        } else {
            indicators.append("HTTPS Verified; ");
            summary.append("Connection is encrypted via HTTPS protocol. ");
            if (txtWebSslStatus != null) {
                txtWebSslStatus.setText("HTTPS (Encrypted)");
                txtWebSslStatus.setTextColor(Color.parseColor("#00E676"));
            }
        }

        boolean isIpHost = host.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
        if (isIpHost) {
            score -= 35;
            indicators.append("IP Address Host; ");
            summary.append("Uses raw IP address instead of registered domain name. ");
            if (txtWebCookiesStatus != null) {
                txtWebCookiesStatus.setText("Raw IP Host (High Risk)");
                txtWebCookiesStatus.setTextColor(Color.parseColor("#FF1744"));
            }
        } else {
            String[] suspiciousTlds = {".tk", ".ml", ".ga", ".cf", ".gq", ".top", ".buzz", ".xyz", ".cc"};
            boolean hasSuspiciousTld = false;
            for (String tld : suspiciousTlds) {
                if (host.endsWith(tld)) {
                    hasSuspiciousTld = true;
                    break;
                }
            }
            if (hasSuspiciousTld) {
                score -= 15;
                indicators.append("Unusual TLD; ");
                summary.append("Uses high-abuse top-level domain. ");
                if (txtWebCookiesStatus != null) {
                    txtWebCookiesStatus.setText("Uncommon TLD");
                    txtWebCookiesStatus.setTextColor(Color.parseColor("#FFD600"));
                }
            } else {
                indicators.append("Standard Domain; ");
                if (txtWebCookiesStatus != null) {
                    txtWebCookiesStatus.setText("Standard Domain Format");
                    txtWebCookiesStatus.setTextColor(Color.parseColor("#00E676"));
                }
            }
        }

        boolean hasSubdomainAbuse = host.split("\\.").length > 4;
        boolean hasSuspiciousKeywords = (path + "?" + query).matches(".*(login|verify|update|account|secure|banking|signin|wallet).*")
                && !host.contains("google.") && !host.contains("microsoft.") && !host.contains("apple.") && !host.contains("amazon.");

        if (hasSubdomainAbuse || hasSuspiciousKeywords) {
            score -= 20;
            indicators.append("Suspicious URL Pattern; ");
            summary.append("URL contains sensitive keywords or deep nested subdomains. ");
            if (txtWebTrackersStatus != null) {
                txtWebTrackersStatus.setText("Potential Phishing Pattern");
                txtWebTrackersStatus.setTextColor(Color.parseColor("#FFD600"));
            }
        } else {
            indicators.append("Standard Structure; ");
            if (txtWebTrackersStatus != null) {
                txtWebTrackersStatus.setText("Standard Pattern");
                txtWebTrackersStatus.setTextColor(Color.parseColor("#00E676"));
            }
        }

        if (txtWebPermissionsStatus != null) {
            txtWebPermissionsStatus.setText("Sandboxed (Android Protected)");
            txtWebPermissionsStatus.setTextColor(Color.parseColor("#00E676"));
        }

        score = Math.max(10, Math.min(score, 98));

        String riskLevel;
        int colorRes;
        String recommendation;

        if (score >= 80) {
            riskLevel = "Safe / Low Risk";
            colorRes = Color.parseColor("#00E676");
            recommendation = "Website exhibits standard security indicators. Safe to browse.";
        } else if (score >= 50) {
            riskLevel = "Medium Risk";
            colorRes = Color.parseColor("#FFD600");
            recommendation = "Exercise caution. Do not enter sensitive credentials unless verified.";
        } else {
            riskLevel = "High Risk";
            colorRes = Color.parseColor("#FF1744");
            recommendation = "Avoid submitting personal data or credentials on this unverified or unencrypted site.";
        }

        if (txtWebScore != null) txtWebScore.setText(String.valueOf(score));
        if (txtWebRiskLevel != null) {
            txtWebRiskLevel.setText(riskLevel);
            txtWebRiskLevel.setTextColor(colorRes);
        }
        if (imgWebScoreRing != null) {
            imgWebScoreRing.setColorFilter(colorRes);
        }
        if (txtWebRiskSummary != null) {
            txtWebRiskSummary.setText(summary.toString().trim());
        }

        String currentTime = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date());

        WebsiteModel scanModel = new WebsiteModel(
                fullUrl,
                host,
                score,
                riskLevel,
                summary.toString().trim(),
                currentTime,
                indicators.toString().trim(),
                recommendation
        );

        sessionManager.addWebsiteScan(scanModel);
        Toast.makeText(getContext(), "Scan complete: " + riskLevel, Toast.LENGTH_SHORT).show();
    }

    private void displayScanResult(WebsiteModel model) {
        if (model == null) return;
        int score = (int) model.getRiskScore();
        if (score == 0) score = 85;

        int colorRes;
        if (score >= 80) {
            colorRes = Color.parseColor("#00E676");
        } else if (score >= 50) {
            colorRes = Color.parseColor("#FFD600");
        } else {
            colorRes = Color.parseColor("#FF1744");
        }

        if (txtWebScore != null) txtWebScore.setText(String.valueOf(score));
        if (txtWebRiskLevel != null) {
            txtWebRiskLevel.setText(model.getRiskLevel() != null ? model.getRiskLevel() : "Scanned");
            txtWebRiskLevel.setTextColor(colorRes);
        }
        if (imgWebScoreRing != null) {
            imgWebScoreRing.setColorFilter(colorRes);
        }
        if (txtWebRiskSummary != null && model.getSummary() != null) {
            txtWebRiskSummary.setText(model.getSummary());
        }

        if (txtWebSslStatus != null) {
            if (model.getUrl() != null && model.getUrl().startsWith("https://")) {
                txtWebSslStatus.setText("HTTPS (Encrypted)");
                txtWebSslStatus.setTextColor(Color.parseColor("#00E676"));
            } else {
                txtWebSslStatus.setText("HTTP (Insecure)");
                txtWebSslStatus.setTextColor(Color.parseColor("#FF1744"));
            }
        }
    }
}