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
        holder.imgAppIcon.setImageTintList(null);
        holder.imgAppIcon.setColorFilter(null);
        try {
            PackageManager pm = context.getPackageManager();
            Drawable icon = pm.getApplicationIcon(app.getPackageName());
            if (icon != null) {
                holder.imgAppIcon.setImageDrawable(icon);
            } else {
                holder.imgAppIcon.setImageResource(R.drawable.ic_apps);
            }
        } catch (Exception e) {
            try {
                PackageManager pm = context.getPackageManager();
                android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(app.getPackageName(), 0);
                Drawable icon = appInfo.loadIcon(pm);
                if (icon != null) {
                    holder.imgAppIcon.setImageDrawable(icon);
                } else {
                    holder.imgAppIcon.setImageResource(R.drawable.ic_apps);
                }
            } catch (Exception ex) {
                holder.imgAppIcon.setImageResource(R.drawable.ic_apps);
            }
        }

        // Authoritative risk level from backend RiskScoreModel
        String riskLevel = com.example.securedroid.fragments.AppsFragment.getAppRiskLevel(context, app);

        if ("HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel)) {
            holder.txtAppRiskBadge.setText("High Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFF3B30);
            holder.txtAppRiskBadge.setBackgroundColor(0x2EFF3B30);
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            holder.txtAppRiskBadge.setText("Medium Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFFC107);
            holder.txtAppRiskBadge.setBackgroundColor(0x2EFFC107);
        } else if ("LOW".equalsIgnoreCase(riskLevel) || "SAFE".equalsIgnoreCase(riskLevel)) {
            holder.txtAppRiskBadge.setText("Low Risk");
            holder.txtAppRiskBadge.setTextColor(0xFF00E676);
            holder.txtAppRiskBadge.setBackgroundColor(0x2E00E676);
        } else {
            holder.txtAppRiskBadge.setText("Auditing...");
            holder.txtAppRiskBadge.setTextColor(0xFF90A4AE);
            holder.txtAppRiskBadge.setBackgroundColor(0x2E90A4AE);
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
