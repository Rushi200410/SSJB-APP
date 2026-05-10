package com.example.ssjb.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.AttendanceRecord;
import com.example.ssjb.data.StudentAttendanceStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceMarkAdapter extends RecyclerView.Adapter<AttendanceMarkAdapter.VH> {
    private final List<StudentAttendanceStat> allItems = new ArrayList<>();
    private final List<StudentAttendanceStat> filteredItems = new ArrayList<>();
    private final Map<Integer, Boolean> presentMap = new HashMap<>();

    public void setItems(List<StudentAttendanceStat> items) {
        allItems.clear();
        allItems.addAll(items);
        filteredItems.clear();
        filteredItems.addAll(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String q = query.toLowerCase(Locale.US).trim();
        filteredItems.clear();
        for (StudentAttendanceStat item : allItems) {
            if (q.isEmpty() || item.name.toLowerCase(Locale.US).contains(q)) {
                filteredItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public Map<Integer, Boolean> getPresentMap() {
        return presentMap;
    }

    public void setPresentSelections(List<AttendanceRecord> records) {
        presentMap.clear();
        for (AttendanceRecord record : records) {
            presentMap.put(record.studentId, record.present);
        }
        notifyDataSetChanged();
    }

    public List<StudentAttendanceStat> getAllItems() {
        return allItems;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_mark, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StudentAttendanceStat item = filteredItems.get(position);
        holder.name.setText(item.name + " (" + item.instrument + ")");
        double percent = item.totalSessions == 0 ? 0 : (item.presentCount * 100.0 / item.totalSessions);
        holder.percent.setText(String.format(Locale.US, "Prev: %.1f%%", percent));
        boolean checked = presentMap.getOrDefault(item.id, true);
        holder.presentCheck.setOnCheckedChangeListener(null);
        holder.presentCheck.setChecked(checked);
        holder.presentCheck.setOnCheckedChangeListener((b, isChecked) -> presentMap.put(item.id, isChecked));
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        TextView percent;
        CheckBox presentCheck;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.markStudentName);
            percent = itemView.findViewById(R.id.markStudentPercent);
            presentCheck = itemView.findViewById(R.id.presentCheckBox);
        }
    }
}
