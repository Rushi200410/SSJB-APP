package com.example.ssjb.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "attendance_records",
        primaryKeys = {"sessionId", "studentId"},
        foreignKeys = {
                @ForeignKey(
                        entity = AttendanceSession.class,
                        parentColumns = "id",
                        childColumns = "sessionId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Student.class,
                        parentColumns = "id",
                        childColumns = "studentId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("studentId")}
)
public class AttendanceRecord {
    public int sessionId;
    public int studentId;
    public boolean present;

    public AttendanceRecord(int sessionId, int studentId, boolean present) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.present = present;
    }
}
