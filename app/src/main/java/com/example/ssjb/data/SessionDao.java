package com.example.ssjb.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface SessionDao {
    @Insert
    long insert(AttendanceSession session);

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    AttendanceSession getById(int id);
}
