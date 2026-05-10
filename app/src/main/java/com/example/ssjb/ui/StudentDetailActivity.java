package com.example.ssjb.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.Student;
import com.example.ssjb.data.StudentAttendanceStat;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.DateUtils;
import com.example.ssjb.util.PdfGenerator;

import java.io.File;
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
        String fullName = buildFullName();
        String text = String.format(
                Locale.getDefault(),
                "Name: %s\nPhone: %s\nInstrument: %s\nAddress: %s\nJoined: %s\nSkill: %s\nLast updated: %s\nBalance: %s\nAttendance (30 days): %.1f%% (%d/%d)",
                fullName,
                safe(student.phoneNumber),
                safe(student.instrument),
                safe(student.address),
                student.joiningDateIso == null || student.joiningDateIso.trim().isEmpty()
                        ? getString(R.string.unknown_value)
                        : DateUtils.formatDisplayDate(student.joiningDateIso),
                safe(student.knowledgeLevel),
                student.lastUpdatedIso == null || student.lastUpdatedIso.trim().isEmpty()
                        ? getString(R.string.unknown_value)
                        : DateUtils.formatDisplayDateTime(student.lastUpdatedIso),
                student.balance == null ? getString(R.string.unknown_value) : String.format(Locale.US, "%.2f", student.balance),
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
        if (student == null) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
            return;
        }
        AppExecutors.db().execute(() -> {
            try {
                File pdf = PdfGenerator.generateStudentSummary(this, student, stat);
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdf);
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("application/pdf");
                send.putExtra(Intent.EXTRA_STREAM, uri);
                send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(send, getString(R.string.share_student))));
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String buildFullName() {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, student.name);
        appendPart(builder, student.middleName);
        appendPart(builder, student.surname);
        return builder.length() == 0 ? getString(R.string.unknown_value) : builder.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (part == null || part.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(part.trim());
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.unknown_value) : value.trim();
    }
}
