package com.example.ssjb.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert
    long insert(Student student);

    @Insert
    List<Long> insertAll(List<Student> students);

    @Update
    int update(Student student);

    @Delete
    int delete(Student student);

    @Query("SELECT * FROM students ORDER BY name")
    List<Student> getAllStudents();

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    Student getById(int id);

    @Query(
            "SELECT s.id, s.name, s.middleName, s.surname, s.instrument, s.balance, " +
                    "SUM(CASE WHEN ass.id IS NOT NULL THEN 1 ELSE 0 END) AS totalSessions, " +
                    "SUM(CASE WHEN ass.id IS NOT NULL AND ar.present = 1 THEN 1 ELSE 0 END) AS presentCount " +
                    "FROM students s " +
                    "LEFT JOIN attendance_records ar ON s.id = ar.studentId " +
                    "LEFT JOIN attendance_sessions ass ON ar.sessionId = ass.id " +
                    "AND ass.dateIso >= :fromDateIso " +
                    "GROUP BY s.id " +
                    "ORDER BY s.name"
    )
    List<StudentAttendanceStat> getStudentStatsFromDate(String fromDateIso);

    @Query(
            "SELECT s.id, s.name, s.middleName, s.surname, s.instrument, s.balance, " +
                    "SUM(CASE WHEN ass.id IS NOT NULL THEN 1 ELSE 0 END) AS totalSessions, " +
                    "SUM(CASE WHEN ass.id IS NOT NULL AND ar.present = 1 THEN 1 ELSE 0 END) AS presentCount " +
                    "FROM students s " +
                    "LEFT JOIN attendance_records ar ON s.id = ar.studentId " +
                    "LEFT JOIN attendance_sessions ass ON ar.sessionId = ass.id " +
                    "AND ass.dateIso >= :fromDateIso " +
                    "WHERE s.id = :studentId " +
                    "GROUP BY s.id"
    )
    StudentAttendanceStat getStudentStatFromDate(int studentId, String fromDateIso);
}
