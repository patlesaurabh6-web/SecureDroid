package com.example.securedroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedroid.R;
import com.example.securedroid.models.TimelineModel;

import java.util.List;

public class TimelineAdapter
        extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<TimelineModel> timelineList;

    public TimelineAdapter(List<TimelineModel> timelineList) {
        this.timelineList = timelineList;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_item_timeline, parent, false);

        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TimelineViewHolder holder,
            int position) {

        TimelineModel item = timelineList.get(position);

        holder.txtTimelineDate.setText(item.getDate());

        holder.txtTimelineScore.setText(
                "Privacy Score : " + item.getPrivacyScore()
        );

        holder.txtRiskBadge.setText(
                item.getRiskLevel()
        );
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    public static class TimelineViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTimelineDate;
        TextView txtTimelineScore;
        TextView txtRiskBadge;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTimelineDate =
                    itemView.findViewById(R.id.txtTimelineDate);

            txtTimelineScore =
                    itemView.findViewById(R.id.txtTimelineScore);

            txtRiskBadge =
                    itemView.findViewById(R.id.txtRiskBadge);
        }
    }
}