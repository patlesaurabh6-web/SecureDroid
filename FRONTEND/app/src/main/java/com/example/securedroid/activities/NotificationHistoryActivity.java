package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class NotificationHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button chipAll, chipHigh, chipMedium, chipLow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        btnBack = findViewById(R.id.btnBack);
        chipAll = findViewById(R.id.chipAll);
        chipHigh = findViewById(R.id.chipHigh);
        chipMedium = findViewById(R.id.chipMedium);
        chipLow = findViewById(R.id.chipLow);

        btnBack.setOnClickListener(v -> finish());

        setupChipListeners();
    }

    private void setupChipListeners() {
        chipAll.setOnClickListener(v -> selectChip(chipAll));
        chipHigh.setOnClickListener(v -> selectChip(chipHigh));
        chipMedium.setOnClickListener(v -> selectChip(chipMedium));
        chipLow.setOnClickListener(v -> selectChip(chipLow));
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
}
