package com.example.securedroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;
import com.example.securedroid.models.AlertModel;
import com.example.securedroid.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button chipAll, chipHigh, chipMedium, chipLow;
    private View cardNotif1, cardNotif2, cardNotif3, cardNotif4, cardNotif5;

    private SessionManager sessionManager;
    private List<AlertModel> allAlerts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        chipAll = findViewById(R.id.chipAll);
        chipHigh = findViewById(R.id.chipHigh);
        chipMedium = findViewById(R.id.chipMedium);
        chipLow = findViewById(R.id.chipLow);

        cardNotif1 = findViewById(R.id.cardNotif1);
        cardNotif2 = findViewById(R.id.cardNotif2);
        cardNotif3 = findViewById(R.id.cardNotif3);
        cardNotif4 = findViewById(R.id.cardNotif4);
        cardNotif5 = findViewById(R.id.cardNotif5);

        btnBack.setOnClickListener(v -> finish());

        loadStoredAlerts();
        setupChipListeners();
    }

    private void loadStoredAlerts() {
        allAlerts = sessionManager.getPrivacyAlerts();
        if (allAlerts == null) {
            allAlerts = new ArrayList<>();
        }

        renderAlertCards(allAlerts);
    }

    private void renderAlertCards(List<AlertModel> alerts) {
        View[] cards = {cardNotif1, cardNotif2, cardNotif3, cardNotif4, cardNotif5};

        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;

            if (i < alerts.size()) {
                card.setVisibility(View.VISIBLE);
                AlertModel alert = alerts.get(i);
                bindCardData(card, alert, i + 1);
            } else {
                card.setVisibility(View.GONE);
            }
        }
    }

    private void bindCardData(View card, AlertModel alert, int index) {
        TextView txtTitle = null;
        TextView txtSubtitle = null;
        TextView txtBadge = null;
        ImageView icon = null;

        if (index == 1) {
            icon = findViewById(R.id.iconNotif1);
        } else if (index == 2) {
            icon = findViewById(R.id.iconNotif2);
        } else if (index == 3) {
            icon = findViewById(R.id.iconNotif3);
        } else if (index == 4) {
            icon = findViewById(R.id.iconNotif4);
        } else if (index == 5) {
            icon = findViewById(R.id.iconNotif5);
        }

        // Find child text views inside card layout
        if (card instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) card;
            View relativeLayout = vg.getChildAt(0);
            if (relativeLayout instanceof ViewGroup) {
                ViewGroup rl = (ViewGroup) relativeLayout;
                for (int c = 0; c < rl.getChildCount(); c++) {
                    View child = rl.getChildAt(c);
                    if (child instanceof ViewGroup) {
                        ViewGroup lin = (ViewGroup) child;
                        if (lin.getChildCount() >= 2) {
                            txtTitle = (TextView) lin.getChildAt(0);
                            txtSubtitle = (TextView) lin.getChildAt(1);
                        }
                    } else if (child instanceof TextView) {
                        txtBadge = (TextView) child;
                    }
                }
            }
        }

        String appName = alert.getAppName() != null ? alert.getAppName() : "App";
        String risk = alert.getCurrentRisk() != null ? alert.getCurrentRisk() : "LOW";
        String time = alert.getTime() != null ? alert.getTime() : "Recent";
        String perm = alert.getPermissionChange() != null ? alert.getPermissionChange() : alert.getMessage();

        if (txtTitle != null) txtTitle.setText(alert.getTitle() != null ? alert.getTitle() : appName);
        if (txtSubtitle != null) txtSubtitle.setText(appName + " • " + perm + " • " + time);
        if (txtBadge != null) {
            txtBadge.setText(risk);
            if (risk.toUpperCase().contains("HIGH")) {
                txtBadge.setTextColor(0xFFFF3B30);
                txtBadge.setBackgroundColor(0x2EFF3B30);
            } else if (risk.toUpperCase().contains("MEDIUM")) {
                txtBadge.setTextColor(0xFFFFC107);
                txtBadge.setBackgroundColor(0x2EFFC107);
            } else {
                txtBadge.setTextColor(0xFF00E676);
                txtBadge.setBackgroundColor(0x2E00E676);
            }
        }

        card.setOnClickListener(v -> openAlertDetails(alert));
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
        if ("ALL".equalsIgnoreCase(severity)) {
            renderAlertCards(allAlerts);
            return;
        }

        List<AlertModel> filtered = new ArrayList<>();
        for (AlertModel alert : allAlerts) {
            String risk = alert.getCurrentRisk() != null ? alert.getCurrentRisk() : alert.getRiskLevel();
            if (risk != null && risk.toUpperCase().contains(severity.toUpperCase())) {
                filtered.add(alert);
            }
        }
        renderAlertCards(filtered);
    }

    private void openAlertDetails(AlertModel alert) {
        Intent intent = new Intent(this, AlertDetailsActivity.class);
        intent.putExtra("APP_NAME", alert.getAppName());
        intent.putExtra("PACKAGE_NAME", alert.getPackageName());
        intent.putExtra("PERMISSION", alert.getPermissionChange());
        intent.putExtra("TIME", alert.getTime());
        intent.putExtra("DESCRIPTION", alert.getExplanation() != null ? alert.getExplanation() : alert.getMessage());
        intent.putExtra("PREV_RISK", alert.getPreviousRisk());
        intent.putExtra("CURR_RISK", alert.getCurrentRisk());
        startActivity(intent);
    }
}
