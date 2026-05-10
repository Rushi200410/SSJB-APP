package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.Student;
import com.example.ssjb.data.StudentAttendanceStat;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.DateUtils;

import java.util.Locale;

public class StudentDetailActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "student_id";
    private AppDatabase db;
    private int studentId;
    private Student student;
    private StudentAttendanceStat stat;

    private TextView detailsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        db = AppDatabase.getInstance(this);
        studentId = getIntent().getIntExtra(EXTRA_STUDENT_ID, -1);
        detailsView = findViewById(R.id.studentDetailText);
        Button editButton = findViewById(R.id.editStudentButton);
        Button deleteButton = findViewById(R.id.deleteStudentButton);
        Button shareButton = findViewById(R.id.shareStudentAttendanceButton);

        editButton.setOnClickListener(v -> {
            Intent i = new Intent(this, StudentFormActivity.class);
            i.putExtra(StudentFormActivity.EXTRA_STUDENT_ID, studentId);
            startActivity(i);
        });

        deleteButton.setOnClickListener(v -> deleteStudent());
        shareButton.setOnClickListener(v -> shareAttendance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        AppExecutors.db().execute(() -> {
            student = db.studentDao().getById(studentId);
            stat = db.studentDao().getStudentStatFromDate(studentId, DateUtils.last30DaysIso());
            runOnUiThread(this::render);
        });
    }

    private void render() {
        if (student == null) {
            finish();
            return;
        }
        int total = stat == null ? 0 : stat.totalSessions;
        int present = stat == null ? 0 : stat.presentCount;
        double percent = total == 0 ? 0 : (present * 100.0 / total);
        String text = String.format(
                Locale.US,
                "Name: %s\nInstrument: %s\nBalance: %s\nAttendance(30d): %.1f%% (%d/%d)",
                student.name,
                student.instrument,
                student.balance == null ? "Not set" : String.format(Locale.US, "%.2f", student.balance),
                percent,
                present,
                total
        );
        detailsView.setText(text);
    }

    private void deleteStudent() {
        AppExecutors.db().execute(() -> {
            if (student != null) {
                db.studentDao().delete(student);
            }
            runOnUiThread(this::finish);
        });
    }

    private void shareAttendance() {
        if (student == null || stat == null) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
            return;
        }
        int total = stat.totalSessions;
        int present = stat.presentCount;
        double percent = total == 0 ? 0 : (present * 100.0 / total);
        String shareText = String.format(
                Locale.US,
                "Student Attendance (Last 30 Days)\nName: %s\nInstrument: %s\nAttendance: %.1f%% (%d/%d)\nBalance: %s",
                student.name,
                student.instrument,
                percent,
                present,
                total,
                student.balance == null ? "Not set" : String.format(Locale.US, "%.2f", student.balance)
        );
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(send, getString(R.string.share_student)));
    }
}
