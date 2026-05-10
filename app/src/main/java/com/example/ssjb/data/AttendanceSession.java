package com.example.ssjb.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance_sessions")
public class AttendanceSession {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String sessionType;

    @NonNull
    public String dateIso;

    @NonNull
    public String dayLabel;

    @NonNull
    public String timeLabel;

    public String classMode;
    public String callTitle;
    public String callDescription;
    public String location;

    public AttendanceSession(
            @NonNull String sessionType,
            @NonNull String dateIso,
            @NonNull String dayLabel,
            @NonNull String timeLabel,
            String classMode,
            String callTitle,
            String callDescription,
            String location
    ) {
        this.sessionType = sessionType;
        this.dateIso = dateIso;
        this.dayLabel = dayLabel;
        this.timeLabel = timeLabel;
        this.classMode = classMode;
        this.callTitle = callTitle;
        this.callDescription = callDescription;
        this.location = location;
    }
}
