package com.example.ssjb.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.AttendanceRecord;
import com.example.ssjb.data.AttendanceSession;
import com.example.ssjb.data.StudentAttendanceStat;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.DateUtils;
import com.example.ssjb.util.PdfGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SessionAttendanceActivity extends AppCompatActivity {
    private AppDatabase db;
    private AttendanceMarkAdapter adapter;
    private String sessionType;
    private String date;
    private String day;
    private String time;
    private String classMode;
    private String callTitle;
    private String callDescription;
    private String callLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_attendance);
        db = AppDatabase.getInstance(this);

        sessionType = getIntent().getStringExtra(SessionFormActivity.EXTRA_SESSION_TYPE);
        date = getIntent().getStringExtra("date");
        day = getIntent().getStringExtra("day");
        time = getIntent().getStringExtra("time");
        classMode = getIntent().getStringExtra("classMode");
        callTitle = getIntent().getStringExtra("callTitle");
        callDescription = getIntent().getStringExtra("callDescription");
        callLocation = getIntent().getStringExtra("callLocation");

        RecyclerView recycler = findViewById(R.id.attendanceRecycler);
        EditText searchInput = findViewById(R.id.searchStudentInput);
        Button submitButton = findViewById(R.id.submitAttendanceButton);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceMarkAdapter();
        recycler.setAdapter(adapter);

        findViewById(R.id.searchButton).setOnClickListener(v ->
                adapter.filter(searchInput.getText().toString())
        );
        submitButton.setOnClickListener(v -> saveSessionAndGeneratePdf());

        loadStudentsSortedByAttendance();
    }

    private void loadStudentsSortedByAttendance() {
        AppExecutors.db().execute(() -> {
            List<StudentAttendanceStat> stats = db.studentDao().getStudentStatsFromDate(DateUtils.last30DaysIso());
            stats.sort((a, b) -> {
                double pa = a.totalSessions == 0 ? 0 : (a.presentCount * 1.0 / a.totalSessions);
                double pb = b.totalSessions == 0 ? 0 : (b.presentCount * 1.0 / b.totalSessions);
                return Double.compare(pb, pa);
            });
            runOnUiThread(() -> adapter.setItems(stats));
        });
    }

    private void saveSessionAndGeneratePdf() {
        AppExecutors.db().execute(() -> {
            try {
                AttendanceSession session = new AttendanceSession(
                        sessionType,
                        date == null || date.isEmpty() ? DateUtils.todayIso() : date,
                        day == null ? "" : day,
                        time == null ? "" : time,
                        classMode,
                        callTitle,
                        callDescription,
                        callLocation
                );
                int sessionId = (int) db.sessionDao().insert(session);
                session.id = sessionId;

                List<AttendanceRecord> records = new ArrayList<>();
                for (StudentAttendanceStat st : adapter.getAllItems()) {
                    boolean present = adapter.getPresentMap().getOrDefault(st.id, true);
                    records.add(new AttendanceRecord(sessionId, st.id, present));
                }
                db.attendanceRecordDao().insertAll(records);
                List<com.example.ssjb.data.SessionStudentRow> rows = db.attendanceRecordDao().getRowsForSession(sessionId);
                File pdf = PdfGenerator.generate(this, session, rows);
                runOnUiThread(() -> sharePdf(pdf));
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void sharePdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_pdf)));
        finish();
    }
}
