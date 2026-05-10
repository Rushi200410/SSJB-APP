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

    private final List<StudentAttendanceStat> items = new ArrayList<>();
    private final OnStudentLongPress onStudentLongPress;

    public StudentListAdapter(OnStudentLongPress onStudentLongPress) {
        this.onStudentLongPress = onStudentLongPress;
    }

    public void setItems(List<StudentAttendanceStat> data) {
        items.clear();
        items.addAll(data);
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
        StudentAttendanceStat item = items.get(position);
        holder.name.setText(item.name);
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
        return items.size();
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
}
