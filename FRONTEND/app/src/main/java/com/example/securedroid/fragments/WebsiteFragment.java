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
import android.widget.LinearLayout;
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
import java.util.List;
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

    private TextView txtWebExplanation;
    private TextView txtWebRecommendation;
    private TextView txtWebHistoryHeader;
    private LinearLayout layoutWebHistoryList;

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

        txtWebExplanation = view.findViewById(R.id.txtWebExplanation);
        txtWebRecommendation = view.findViewById(R.id.txtWebRecommendation);
        txtWebHistoryHeader = view.findViewById(R.id.txtWebHistoryHeader);
        layoutWebHistoryList = view.findViewById(R.id.layoutWebHistoryList);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> scanWebsite());
        }

        loadLatestScanIfExists();
        renderHistoryList();
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

    private void renderHistoryList() {
        if (layoutWebHistoryList == null || sessionManager == null) return;
        layoutWebHistoryList.removeAllViews();

        List<WebsiteModel> history = sessionManager.getWebsiteScans();
        if (history == null || history.isEmpty()) {
            if (txtWebHistoryHeader != null) txtWebHistoryHeader.setVisibility(View.GONE);
            return;
        }

        if (txtWebHistoryHeader != null) txtWebHistoryHeader.setVisibility(View.VISIBLE);

        for (WebsiteModel item : history) {
            LinearLayout itemRow = new LinearLayout(getContext());
            itemRow.setOrientation(LinearLayout.VERTICAL);
            itemRow.setBackgroundResource(R.drawable.bg_glass_card);
            itemRow.setPadding(
                    (int) (14 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (14 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density)
            );
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, (int) (10 * getResources().getDisplayMetrics().density));
            itemRow.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(getContext());
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView txtUrl = new TextView(getContext());
            txtUrl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            txtUrl.setText(item.getUrl());
            txtUrl.setTextColor(Color.WHITE);
            txtUrl.setTextSize(14);
            txtUrl.setTypeface(null, android.graphics.Typeface.BOLD);
            txtUrl.setSingleLine(true);
            txtUrl.setEllipsize(android.text.TextUtils.TruncateAt.END);

            int score = (int) item.getRiskScore();
            TextView txtBadge = new TextView(getContext());
            txtBadge.setText(item.getRiskLevel() != null ? item.getRiskLevel() + " (" + score + ")" : score + "/100");
            txtBadge.setTextSize(11);
            txtBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            txtBadge.setPadding(
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (3 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (3 * getResources().getDisplayMetrics().density)
            );

            if (score >= 80) {
                txtBadge.setTextColor(Color.parseColor("#00E676"));
                txtBadge.setBackgroundColor(0x2E00E676);
            } else if (score >= 50) {
                txtBadge.setTextColor(Color.parseColor("#FFD600"));
                txtBadge.setBackgroundColor(0x2EFFD600);
            } else {
                txtBadge.setTextColor(Color.parseColor("#FF1744"));
                txtBadge.setBackgroundColor(0x2EFF1744);
            }

            topRow.addView(txtUrl);
            topRow.addView(txtBadge);

            TextView txtTime = new TextView(getContext());
            txtTime.setText(item.getDateTime() != null ? item.getDateTime() : "Scanned");
            txtTime.setTextColor(Color.parseColor("#8E9AA8"));
            txtTime.setTextSize(11);
            txtTime.setPadding(0, (int) (4 * getResources().getDisplayMetrics().density), 0, 0);

            itemRow.addView(topRow);
            itemRow.addView(txtTime);

            itemRow.setOnClickListener(v -> {
                if (etWebsiteUrl != null) etWebsiteUrl.setText(item.getUrl());
                displayScanResult(item);
                Toast.makeText(getContext(), "Loaded scan for " + item.getDomain(), Toast.LENGTH_SHORT).show();
            });

            layoutWebHistoryList.addView(itemRow);
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
        String query = uri.getQuery() != null ? queryStr(uri) : "";

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
            recommendation = "• Connection is encrypted.\n• Domain format is standard.\n• Safe for regular web browsing.";
        } else if (score >= 50) {
            riskLevel = "Medium Risk";
            colorRes = Color.parseColor("#FFD600");
            recommendation = "• Exercise caution.\n• Do not enter bank/personal credentials unless domain is independently verified.";
        } else {
            riskLevel = "High Risk";
            colorRes = Color.parseColor("#FF1744");
            recommendation = "• Avoid submitting passwords or credit card information.\n• Unencrypted or high-risk domain structure detected.";
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
        if (txtWebExplanation != null) {
            txtWebExplanation.setText(summary.toString().trim() + "\nIndicators: " + indicators.toString().trim());
        }
        if (txtWebRecommendation != null) {
            txtWebRecommendation.setText(recommendation);
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
        renderHistoryList();
        Toast.makeText(getContext(), "Scan complete: " + riskLevel, Toast.LENGTH_SHORT).show();
    }

    private String queryStr(Uri uri) {
        try {
            return uri.getQuery() != null ? uri.getQuery().toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void displayScanResult(WebsiteModel model) {
        if (model == null) return;
        int score = (int) model.getRiskScore();

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
        if (txtWebExplanation != null) {
            String exp = (model.getSummary() != null ? model.getSummary() : "") +
                    (model.getDetectedIndicators() != null ? "\nIndicators: " + model.getDetectedIndicators() : "");
            txtWebExplanation.setText(exp.trim());
        }
        if (txtWebRecommendation != null && model.getRecommendation() != null) {
            txtWebRecommendation.setText(model.getRecommendation());
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