package com.example.securedroid.adapters;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class LiveMonitorAppAdapter extends RecyclerView.Adapter<LiveMonitorAppAdapter.ViewHolder> {

    public static class RunningAppItem {
        private final String appName;
        private final String packageName;
        private final String status;
        private final String riskLevel;
        private final Drawable icon;

        public RunningAppItem(String appName, String packageName, String status, String riskLevel, Drawable icon) {
            this.appName = appName;
            this.packageName = packageName;
            this.status = status;
            this.riskLevel = riskLevel;
            this.icon = icon;
        }

        public String getAppName() { return appName; }
        public String getPackageName() { return packageName; }
        public String getStatus() { return status; }
        public String getRiskLevel() { return riskLevel; }
        public Drawable getIcon() { return icon; }
    }

    private final Context context;
    private final List<RunningAppItem> items;

    public LiveMonitorAppAdapter(Context context, List<RunningAppItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_live_monitor_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RunningAppItem item = items.get(position);

        holder.txtAppName.setText(item.getAppName());
        holder.txtAppStatus.setText(item.getStatus());

        // Always display actual native app logo without any tint or color filter
        holder.imgAppLogo.setImageTintList(null);
        holder.imgAppLogo.setColorFilter(null);
        if (item.getIcon() != null) {
            holder.imgAppLogo.setImageDrawable(item.getIcon());
        } else {
            holder.imgAppLogo.setImageResource(R.drawable.ic_apps);
        }

        String risk = item.getRiskLevel() != null ? item.getRiskLevel().toUpperCase() : "LOW";
        if ("HIGH".equalsIgnoreCase(risk) || "CRITICAL".equalsIgnoreCase(risk)) {
            holder.txtAppRiskBadge.setText("High Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFF3B30);
            holder.txtAppRiskBadge.setBackgroundColor(0x28FF3B30);
        } else if ("MEDIUM".equalsIgnoreCase(risk)) {
            holder.txtAppRiskBadge.setText("Medium Risk");
            holder.txtAppRiskBadge.setTextColor(0xFFFFC107);
            holder.txtAppRiskBadge.setBackgroundColor(0x28FFC107);
        } else {
            holder.txtAppRiskBadge.setText("Low Risk");
            holder.txtAppRiskBadge.setTextColor(0xFF00E676);
            holder.txtAppRiskBadge.setBackgroundColor(0x2800E676);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppDetailsActivity.class);
            intent.putExtra("APP_NAME", item.getAppName());
            intent.putExtra("PACKAGE_NAME", item.getPackageName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppLogo;
        TextView txtAppName, txtAppStatus, txtAppRiskBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAppLogo = itemView.findViewById(R.id.imgAppLogo);
            txtAppName = itemView.findViewById(R.id.txtAppName);
            txtAppStatus = itemView.findViewById(R.id.txtAppStatus);
            txtAppRiskBadge = itemView.findViewById(R.id.txtAppRiskBadge);
        }
    }
}
