package com.example.securedroid.activities.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.securedroid.R;
import com.example.securedroid.activities.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;
    TextView title, subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);

        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scale = new ScaleAnimation(
                0.5f,
                1f,
                0.5f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        scale.setDuration(1200);
        scale.setInterpolator(new OvershootInterpolator());

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(1200);

        animationSet.addAnimation(scale);
        animationSet.addAnimation(fade);

        if (logo != null) logo.startAnimation(animationSet);
        if (title != null) title.startAnimation(fade);
        if (subtitle != null) subtitle.startAnimation(fade);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}