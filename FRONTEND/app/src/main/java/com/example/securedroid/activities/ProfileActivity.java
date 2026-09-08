package com.example.securedroid.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securedroid.R;
import com.example.securedroid.fragments.ProfileFragment;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ProfileFragment())
                    .commit();
        }
    }
}