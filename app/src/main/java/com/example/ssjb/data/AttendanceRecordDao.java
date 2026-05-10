package com.example.ssjb.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AttendanceRecordDao {
    @Insert
    void insertAll(List<AttendanceRecord> records);

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    void deleteBySessionId(int sessionId);

    @Query(
            "SELECT s.name, ar.present FROM attendance_records ar " +
                    "INNER JOIN students s ON s.id = ar.studentId " +
                    "WHERE ar.sessionId = :sessionId ORDER BY s.name"
    )
    List<SessionStudentRow> getRowsForSession(int sessionId);

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    List<AttendanceRecord> getRecordsForSession(int sessionId);
}
