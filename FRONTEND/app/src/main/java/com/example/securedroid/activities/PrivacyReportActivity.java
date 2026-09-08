package com.example.securedroid.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;

public class PrivacyReportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerTimeRange;
    private Button btnExportPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_report);

        btnBack = findViewById(R.id.btnBack);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        btnBack.setOnClickListener(v -> finish());

        String[] options = {"This Week", "Last Week", "This Month", "All Time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerTimeRange.setAdapter(adapter);

        btnExportPdf.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting Privacy Report PDF...", Toast.LENGTH_LONG).show();
        });
    }
}
