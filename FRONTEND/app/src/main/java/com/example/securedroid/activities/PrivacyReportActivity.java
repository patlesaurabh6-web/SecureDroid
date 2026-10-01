package com.example.securedroid.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.securedroid.R;
import com.example.securedroid.fragments.AppsFragment;
import com.example.securedroid.models.AlertModel;
import com.example.securedroid.models.AppModel;
import com.example.securedroid.models.WebsiteModel;
import com.example.securedroid.utils.PackageManagerHelper;
import com.example.securedroid.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrivacyReportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerTimeRange;
    private Button btnExportPdf;

    private TextView txtReportScore, txtReportScoreStatus;
    private TextView txtReportTotalApps, txtReportHighRisk, txtReportAlerts;

    private SessionManager sessionManager;
    private List<AppModel> installedApps;
    private List<AlertModel> storedAlerts;
    private List<WebsiteModel> websiteScans;

    private int totalApps = 0;
    private int safeApps = 0;
    private int mediumRiskApps = 0;
    private int highRiskApps = 0;
    private int privacyScore = 85;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_report);

        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        txtReportScore = findViewById(R.id.txtReportScore);
        txtReportScoreStatus = findViewById(R.id.txtReportScoreStatus);
        txtReportTotalApps = findViewById(R.id.txtReportTotalApps);
        txtReportHighRisk = findViewById(R.id.txtReportHighRisk);
        txtReportAlerts = findViewById(R.id.txtReportAlerts);

        btnBack.setOnClickListener(v -> finish());

        String[] options = {"All Time (Current Device Audit)", "This Month", "This Week"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerTimeRange.setAdapter(adapter);

        loadReportData();

        btnExportPdf.setOnClickListener(v -> generateAndSharePdfReport());
    }

    private void loadReportData() {
        installedApps = PackageManagerHelper.getInstalledApps(this);
        storedAlerts = sessionManager.getPrivacyAlerts();
        websiteScans = sessionManager.getWebsiteScans();

        totalApps = installedApps.size();
        safeApps = 0;
        mediumRiskApps = 0;
        highRiskApps = 0;

        for (AppModel app : installedApps) {
            String cat = AppsFragment.getAppRiskCategory(app);
            if ("HIGH".equals(cat)) highRiskApps++;
            else if ("MEDIUM".equals(cat)) mediumRiskApps++;
            else safeApps++;
        }

        privacyScore = totalApps > 0 ? Math.max(15, 100 - (highRiskApps * 10) - (mediumRiskApps * 3)) : 85;

        if (txtReportScore != null) txtReportScore.setText(String.valueOf(privacyScore));
        if (txtReportTotalApps != null) txtReportTotalApps.setText(String.valueOf(totalApps));
        if (txtReportHighRisk != null) txtReportHighRisk.setText(String.valueOf(highRiskApps));
        if (txtReportAlerts != null) txtReportAlerts.setText(String.valueOf(storedAlerts != null ? storedAlerts.size() : 0));

        if (txtReportScoreStatus != null) {
            if (privacyScore >= 70) {
                txtReportScoreStatus.setText("  Low Privacy Risk");
                txtReportScoreStatus.setTextColor(0xFF00E676);
            } else if (privacyScore >= 40) {
                txtReportScoreStatus.setText("  Moderate Privacy Risk");
                txtReportScoreStatus.setTextColor(0xFFFFC107);
            } else {
                txtReportScoreStatus.setText("  High Privacy Risk Detected");
                txtReportScoreStatus.setTextColor(0xFFFF3B30);
            }
        }
    }

    private void generateAndSharePdfReport() {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Background
            paint.setColor(Color.WHITE);
            canvas.drawRect(0, 0, 595, 842, paint);

            // Title
            paint.setColor(Color.parseColor("#0F172A"));
            paint.setTextSize(20);
            paint.setFakeBoldText(true);
            canvas.drawText("SecureDroid Privacy Audit Report", 40, 50, paint);

            // Timestamp
            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            paint.setColor(Color.DKGRAY);
            String reportDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated on: " + reportDate, 40, 70, paint);

            // Section 1: Executive Summary
            paint.setColor(Color.parseColor("#1E80FF"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("1. Device Privacy Summary", 40, 110, paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(11);
            paint.setFakeBoldText(false);
            canvas.drawText("• Privacy Health Score: " + privacyScore + " / 100", 50, 135, paint);
            canvas.drawText("• Total Applications Scanned: " + totalApps, 50, 155, paint);
            canvas.drawText("• Safe / Low Risk Applications: " + safeApps, 50, 175, paint);
            canvas.drawText("• Medium Risk Applications: " + mediumRiskApps, 50, 195, paint);
            canvas.drawText("• High Risk Applications: " + highRiskApps, 50, 215, paint);
            canvas.drawText("• Logged Privacy Events / Changes: " + (storedAlerts != null ? storedAlerts.size() : 0), 50, 235, paint);
            canvas.drawText("• Recorded Website Scans: " + (websiteScans != null ? websiteScans.size() : 0), 50, 255, paint);

            // Section 2: High Risk Applications
            paint.setColor(Color.parseColor("#1E80FF"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("2. High Risk Applications Requiring Audit", 40, 295, paint);

            int y = 320;
            paint.setColor(Color.BLACK);
            paint.setTextSize(10);
            paint.setFakeBoldText(false);

            int listedCount = 0;
            for (AppModel app : installedApps) {
                if ("HIGH".equals(AppsFragment.getAppRiskCategory(app))) {
                    canvas.drawText("• " + app.getApplicationName() + " (" + app.getPackageName() + ")", 50, y, paint);
                    y += 18;
                    listedCount++;
                    if (listedCount >= 8) break;
                }
            }
            if (listedCount == 0) {
                canvas.drawText("• No high risk applications detected on device.", 50, y, paint);
                y += 18;
            }

            // Section 3: Recommendations
            y += 20;
            paint.setColor(Color.parseColor("#1E80FF"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("3. Privacy Recommendations", 40, y, paint);

            y += 25;
            paint.setColor(Color.BLACK);
            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            canvas.drawText("1. Review Camera, Microphone, and Location permissions for apps categorized as High Risk.", 50, y, paint);
            y += 18;
            canvas.drawText("2. Revoke background location access for non-navigation apps in Android Settings.", 50, y, paint);
            y += 18;
            canvas.drawText("3. Ensure URLs scanned with unencrypted HTTP are not used for entering credentials.", 50, y, paint);
            y += 18;
            canvas.drawText("4. Keep periodic live monitoring active in SecureDroid to capture permission modifications.", 50, y, paint);

            document.finishPage(page);

            File reportFile = new File(getExternalFilesDir(null), "SecureDroid_Privacy_Report.pdf");
            FileOutputStream fos = new FileOutputStream(reportFile);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "Privacy Report PDF exported: " + reportFile.getName(), Toast.LENGTH_SHORT).show();

            try {
                Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", reportFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Privacy Report PDF"));
            } catch (Exception e) {
                // If FileProvider is not declared, open regular toast
                Toast.makeText(this, "Report saved to: " + reportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF Generation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
