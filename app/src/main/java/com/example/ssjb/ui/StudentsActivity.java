package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
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
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.studentsRecycler);
        searchInput = findViewById(R.id.searchStudentInput);
        ImageButton addButton = findViewById(R.id.addStudentButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentListAdapter(student -> {
            Intent i = new Intent(this, StudentDetailActivity.class);
            i.putExtra(StudentDetailActivity.EXTRA_STUDENT_ID, student.id);
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> startActivity(new Intent(this, StudentFormActivity.class)));
        searchInput.addTextChangedListener(new SimpleTextWatcher(text -> adapter.filter(text)));
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

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<String> onChanged;

        SimpleTextWatcher(java.util.function.Consumer<String> onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.accept(s == null ? "" : s.toString());
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}
