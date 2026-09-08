package com.example.securedroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.securedroid.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class LiveMonitorFragment extends Fragment {

    private SwitchMaterial switchLiveMonitor;
    private TextView txtProtectionStatus;
    private ImageView imgRadarGlow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_monitor, container, false);

        switchLiveMonitor = view.findViewById(R.id.switchLiveMonitor);
        txtProtectionStatus = view.findViewById(R.id.txtProtectionStatus);
        imgRadarGlow = view.findViewById(R.id.imgRadarGlow);

        startRadarAnimation();

        switchLiveMonitor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtProtectionStatus.setText("Real-time protection is ON");
                txtProtectionStatus.setTextColor(0xFF00E676);
                startRadarAnimation();
            } else {
                txtProtectionStatus.setText("Real-time protection is OFF");
                txtProtectionStatus.setTextColor(0xFFFF3B30);
                if (imgRadarGlow != null) {
                    imgRadarGlow.clearAnimation();
                }
            }
        });

        return view;
    }

    private void startRadarAnimation() {
        if (imgRadarGlow != null) {
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(4000);
            rotate.setRepeatCount(Animation.INFINITE);
            imgRadarGlow.startAnimation(rotate);
        }
    }
}
