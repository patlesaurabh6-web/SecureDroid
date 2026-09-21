package com.example.securedroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedroid.R;
import com.example.securedroid.activities.AppDetailsActivity;
import com.example.securedroid.models.AppModel;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private final Context context;
    private final List<AppModel> appList;

    public AppAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel app = appList.get(position);

        holder.txtAppName.setText(app.getApplicationName());

        int permCount = app.getPermissions() != null ? app.getPermissions().size() : 0;
        holder.txtAppPermissions.setText(permCount + " Permissions Requested");

        // Load actual app icon
        try {
            PackageManager pm = context.getPackageManager();
            Drawable icon = pm.getApplicationIcon(app.getPackageName());
            holder.imgAppIcon.setImageDrawable(icon);
            holder.imgAppIcon.setColorFilter(null); // Clear tint for real icon
        } catch (Exception e) {
            holder.imgAppIcon.setImageResource(R.drawable.ic_apps);
        }

        // Calculate dynamic risk level
        boolean hasCamera = false;
        boolean hasMic = false;
        boolean hasLocation = false;

        if (app.getPermissions() != null) {
            for (String p : app.getPermissions()) {
                if (p.contains("CAMERA")) hasCamera = true;
                if (p.contains("RECORD_AUDIO")) hasMic = true;
                if (p.contains("ACCESS_FINE_LOCATION") || p.contains("ACCESS_COARSE_LOCATION")) hasLocation = true;
            }
        }

        if (hasCamera && hasLocation) {
            holder.txtAppRiskBadge.setText("High Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFF3B30);
            holder.txtAppRiskBadge.setBackgroundColor(0x2EFF3B30);
        } else if (hasCamera || hasMic || hasLocation) {
            holder.txtAppRiskBadge.setText("Medium Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFFC107);
            holder.txtAppRiskBadge.setBackgroundColor(0x2EFFC107);
        } else {
            holder.txtAppRiskBadge.setText("Low Risk");
            holder.txtAppRiskBadge.setTextColor(0xFF00E676);
            holder.txtAppRiskBadge.setBackgroundColor(0x2E00E676);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppDetailsActivity.class);
            intent.putExtra("APP_NAME", app.getApplicationName());
            intent.putExtra("PACKAGE_NAME", app.getPackageName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return appList != null ? appList.size() : 0;
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppIcon;
        TextView txtAppName, txtAppPermissions, txtAppRiskBadge;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
            txtAppName = itemView.findViewById(R.id.txtAppName);
            txtAppPermissions = itemView.findViewById(R.id.txtAppPermissions);
            txtAppRiskBadge = itemView.findViewById(R.id.txtAppRiskBadge);
        }
    }
}
