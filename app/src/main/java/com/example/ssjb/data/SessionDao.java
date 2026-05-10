package com.example.ssjb.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SessionDao {
    @Insert
    long insert(AttendanceSession session);

    @Update
    int update(AttendanceSession session);

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    AttendanceSession getById(int id);

    @Query(
            "SELECT s.id, s.sessionType, s.dateIso, s.dayLabel, s.timeLabel, s.classMode, " +
                    "s.callTitle, s.callDescription, s.location, " +
                    "SUM(CASE WHEN ar.present = 1 THEN 1 ELSE 0 END) AS presentCount, " +
                    "COUNT(ar.studentId) AS totalCount " +
                    "FROM attendance_sessions s " +
                    "LEFT JOIN attendance_records ar ON ar.sessionId = s.id " +
                    "GROUP BY s.id " +
                    "ORDER BY s.dateIso DESC, s.timeLabel DESC, s.id DESC"
    )
    List<SessionHistoryRow> getHistory();
}
