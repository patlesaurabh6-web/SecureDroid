package com.example.securedroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securedroid.R;
import com.example.securedroid.models.AlertModel;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final List<AlertModel> alertList;

    public AlertAdapter(List<AlertModel> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertModel alert = alertList.get(position);
        holder.txtAlertTitle.setText(alert.getTitle());
        
        // Subtitle format: Message • Time
        String subtitle = alert.getMessage() + " • " + alert.getTime();
        holder.txtAlertSubtitle.setText(subtitle);
        
        // Adjust icon color based on risk level
        int colorResId;
        if ("High".equalsIgnoreCase(alert.getRiskLevel())) {
            colorResId = android.R.color.holo_red_light;
        } else if ("Medium".equalsIgnoreCase(alert.getRiskLevel())) {
            colorResId = android.R.color.holo_orange_light;
        } else {
            colorResId = android.R.color.holo_blue_light;
        }
        holder.imgAlert.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), colorResId));
    }

    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAlert;
        TextView txtAlertTitle, txtAlertSubtitle;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlert = itemView.findViewById(R.id.imgAlert);
            txtAlertTitle = itemView.findViewById(R.id.txtAlertTitle);
            txtAlertSubtitle = itemView.findViewById(R.id.txtAlertSubtitle);
        }
    }
}
