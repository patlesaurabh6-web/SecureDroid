package com.example.securedroid.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;
import com.example.securedroid.fragments.WebsiteFragment;

public class WebsiteAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website_analysis);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new WebsiteFragment())
                    .commit();
        }
    }
}