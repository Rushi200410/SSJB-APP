package com.example.ssjb.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.StudentAttendanceStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.VH> {
    public interface OnStudentLongPress {
        void onLongPress(StudentAttendanceStat student);
    }

    private final List<StudentAttendanceStat> allItems = new ArrayList<>();
    private final List<StudentAttendanceStat> visibleItems = new ArrayList<>();
    private final OnStudentLongPress onStudentLongPress;

    public StudentListAdapter(OnStudentLongPress onStudentLongPress) {
        this.onStudentLongPress = onStudentLongPress;
    }

    public void setItems(List<StudentAttendanceStat> data) {
        allItems.clear();
        allItems.addAll(data);
        visibleItems.clear();
        visibleItems.addAll(data);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.US).trim();
        visibleItems.clear();
        for (StudentAttendanceStat item : allItems) {
            if (matches(item, q)) {
                visibleItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StudentAttendanceStat item = visibleItems.get(position);
        holder.name.setText(buildFullName(item));
        holder.instrument.setText(item.instrument);
        double percent = item.totalSessions == 0 ? 0 : (item.presentCount * 100.0 / item.totalSessions);
        holder.percent.setText(String.format(Locale.US, "%.1f%% (30d)", percent));
        holder.itemView.setOnLongClickListener(v -> {
            onStudentLongPress.onLongPress(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        TextView instrument;
        TextView percent;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.studentName);
            instrument = itemView.findViewById(R.id.studentInstrument);
            percent = itemView.findViewById(R.id.studentPercent);
        }
    }

    private boolean matches(StudentAttendanceStat item, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return buildFullName(item).toLowerCase(Locale.US).contains(query)
                || safe(item.instrument).toLowerCase(Locale.US).contains(query);
    }

    private String buildFullName(StudentAttendanceStat item) {
        StringBuilder builder = new StringBuilder();
        append(builder, item.name);
        append(builder, item.middleName);
        append(builder, item.surname);
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
