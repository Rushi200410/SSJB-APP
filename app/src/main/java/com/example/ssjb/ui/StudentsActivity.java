package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.DateUtils;

public class StudentsActivity extends AppCompatActivity {
    private StudentListAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.studentsRecycler);
        ImageButton addButton = findViewById(R.id.addStudentButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentListAdapter(student -> {
            Intent i = new Intent(this, StudentDetailActivity.class);
            i.putExtra(StudentDetailActivity.EXTRA_STUDENT_ID, student.id);
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> startActivity(new Intent(this, StudentFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    private void loadStudents() {
        AppExecutors.db().execute(() -> {
            String fromDate = DateUtils.last30DaysIso();
            var stats = db.studentDao().getStudentStatsFromDate(fromDate);
            runOnUiThread(() -> adapter.setItems(stats));
        });
    }
}
