package com.example.ssjb.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.SessionHistoryRow;
import com.example.ssjb.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.VH> {
    public interface OnSessionOpen {
        void onOpen(SessionHistoryRow row);
    }

    private final List<SessionHistoryRow> items = new ArrayList<>();
    private final OnSessionOpen onSessionOpen;

    public SessionHistoryAdapter(OnSessionOpen onSessionOpen) {
        this.onSessionOpen = onSessionOpen;
    }

    public void setItems(List<SessionHistoryRow> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_history, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionHistoryRow item = items.get(position);
        holder.type.setText("CLASS".equals(item.sessionType)
                ? holder.itemView.getContext().getString(R.string.session_type_class)
                : holder.itemView.getContext().getString(R.string.session_type_call));
        holder.title.setText("CLASS".equals(item.sessionType)
                ? safe(item.classMode)
                : safe(item.callTitle));
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.session_meta,
                DateUtils.formatDisplayDate(item.dateIso),
                safe(item.timeLabel)
        ));
        holder.counts.setText(holder.itemView.getContext().getString(R.string.session_counts, item.presentCount, item.totalCount));
        holder.detail.setText("CLASS".equals(item.sessionType)
                ? safe(item.classMode)
                : buildCallDetail(item));
        holder.openButton.setOnClickListener(v -> onSessionOpen.onOpen(item));
        holder.itemView.setOnClickListener(v -> onSessionOpen.onOpen(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView type;
        TextView title;
        TextView meta;
        TextView counts;
        TextView detail;
        Button openButton;

        VH(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.historyType);
            title = itemView.findViewById(R.id.historyTitle);
            meta = itemView.findViewById(R.id.historyMeta);
            counts = itemView.findViewById(R.id.historyCounts);
            detail = itemView.findViewById(R.id.historyDetail);
            openButton = itemView.findViewById(R.id.openHistoryButton);
        }
    }

    private String buildCallDetail(SessionHistoryRow item) {
        StringBuilder builder = new StringBuilder();
        append(builder, item.callDescription);
        append(builder, item.location);
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(value.trim());
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
