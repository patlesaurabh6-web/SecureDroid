package com.example.securedroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class NotificationHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button chipAll, chipHigh, chipMedium, chipLow;
    private View cardNotif1, cardNotif2, cardNotif3, cardNotif4, cardNotif5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        btnBack = findViewById(R.id.btnBack);
        chipAll = findViewById(R.id.chipAll);
        chipHigh = findViewById(R.id.chipHigh);
        chipMedium = findViewById(R.id.chipMedium);
        chipLow = findViewById(R.id.chipLow);

        cardNotif1 = findViewById(R.id.cardNotif1); // Camera (High)
        cardNotif2 = findViewById(R.id.cardNotif2); // Location (High)
        cardNotif3 = findViewById(R.id.cardNotif3); // Microphone (Medium)
        cardNotif4 = findViewById(R.id.cardNotif4); // Screen Recording (High)
        cardNotif5 = findViewById(R.id.cardNotif5); // Clipboard (Low)

        btnBack.setOnClickListener(v -> finish());

        setupChipListeners();
        setupCardClicks();
    }

    private void setupChipListeners() {
        chipAll.setOnClickListener(v -> {
            selectChip(chipAll);
            filterNotifications("ALL");
        });
        chipHigh.setOnClickListener(v -> {
            selectChip(chipHigh);
            filterNotifications("HIGH");
        });
        chipMedium.setOnClickListener(v -> {
            selectChip(chipMedium);
            filterNotifications("MEDIUM");
        });
        chipLow.setOnClickListener(v -> {
            selectChip(chipLow);
            filterNotifications("LOW");
        });
    }

    private void selectChip(Button selectedChip) {
        Button[] chips = {chipAll, chipHigh, chipMedium, chipLow};
        for (Button chip : chips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_glow_button);
                chip.setTextColor(0xFFFFFFFF);
            } else {
                chip.setBackgroundResource(R.drawable.bg_outline_button);
                chip.setTextColor(0xFFB5C1D8);
            }
        }
    }

    private void filterNotifications(String severity) {
        switch (severity) {
            case "HIGH":
                if (cardNotif1 != null) cardNotif1.setVisibility(android.view.View.VISIBLE);
                if (cardNotif2 != null) cardNotif2.setVisibility(android.view.View.VISIBLE);
                if (cardNotif3 != null) cardNotif3.setVisibility(android.view.View.GONE);
                if (cardNotif4 != null) cardNotif4.setVisibility(android.view.View.VISIBLE);
                if (cardNotif5 != null) cardNotif5.setVisibility(android.view.View.GONE);
                break;
            case "MEDIUM":
                if (cardNotif1 != null) cardNotif1.setVisibility(android.view.View.GONE);
                if (cardNotif2 != null) cardNotif2.setVisibility(android.view.View.GONE);
                if (cardNotif3 != null) cardNotif3.setVisibility(android.view.View.VISIBLE);
                if (cardNotif4 != null) cardNotif4.setVisibility(android.view.View.GONE);
                if (cardNotif5 != null) cardNotif5.setVisibility(android.view.View.GONE);
                break;
            case "LOW":
                if (cardNotif1 != null) cardNotif1.setVisibility(android.view.View.GONE);
                if (cardNotif2 != null) cardNotif2.setVisibility(android.view.View.GONE);
                if (cardNotif3 != null) cardNotif3.setVisibility(android.view.View.GONE);
                if (cardNotif4 != null) cardNotif4.setVisibility(android.view.View.GONE);
                if (cardNotif5 != null) cardNotif5.setVisibility(android.view.View.VISIBLE);
                break;
            case "ALL":
            default:
                if (cardNotif1 != null) cardNotif1.setVisibility(android.view.View.VISIBLE);
                if (cardNotif2 != null) cardNotif2.setVisibility(android.view.View.VISIBLE);
                if (cardNotif3 != null) cardNotif3.setVisibility(android.view.View.VISIBLE);
                if (cardNotif4 != null) cardNotif4.setVisibility(android.view.View.VISIBLE);
                if (cardNotif5 != null) cardNotif5.setVisibility(android.view.View.VISIBLE);
                break;
        }
    }

    private void setupCardClicks() {
        if (cardNotif1 != null) {
            cardNotif1.setOnClickListener(v -> openAlertDetails("Instagram", "Camera Access", "High", "Instagram accessed camera sensor in background."));
        }
        if (cardNotif2 != null) {
            cardNotif2.setOnClickListener(v -> openAlertDetails("Maps", "Precise Location", "High", "Maps accessed precise GPS location coordinates."));
        }
        if (cardNotif3 != null) {
            cardNotif3.setOnClickListener(v -> openAlertDetails("WhatsApp", "Microphone Recording", "Medium", "WhatsApp accessed microphone for voice recording."));
        }
        if (cardNotif4 != null) {
            cardNotif4.setOnClickListener(v -> openAlertDetails("Screen Capture", "Screen Overlay & Recording", "High", "Screen overlay activity and recording detected."));
        }
        if (cardNotif5 != null) {
            cardNotif5.setOnClickListener(v -> openAlertDetails("Clipboard Monitor", "Clipboard Read", "Low", "Clipboard buffer read by background process."));
        }
    }

    private void openAlertDetails(String appName, String perm, String risk, String desc) {
        android.content.Intent intent = new android.content.Intent(this, AlertDetailsActivity.class);
        intent.putExtra("APP_NAME", appName);
        intent.putExtra("PERMISSION", perm);
        intent.putExtra("RISK_LEVEL", risk);
        intent.putExtra("DESCRIPTION", desc);
        startActivity(intent);
    }
}
