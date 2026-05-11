package com.example.ssjb.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.Student;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.CsvImportUtil;
import com.example.ssjb.util.DateUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StudentsActivity extends AppCompatActivity {
    private StudentListAdapter adapter;
    private AppDatabase db;
    private EditText searchInput;
    private ActivityResultLauncher<String[]> importCsvLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.studentsRecycler);
        searchInput = findViewById(R.id.searchStudentInput);
        ImageButton addButton = findViewById(R.id.addStudentButton);
        android.widget.Button importCsvButton = findViewById(R.id.importCsvButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentListAdapter(student -> {
            Intent i = new Intent(this, StudentDetailActivity.class);
            i.putExtra(StudentDetailActivity.EXTRA_STUDENT_ID, student.id);
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> startActivity(new Intent(this, StudentFormActivity.class)));
        searchInput.addTextChangedListener(new SimpleTextWatcher(text -> adapter.filter(text)));

        importCsvLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::importCsvFromUri);
        importCsvButton.setOnClickListener(v -> importCsvLauncher.launch(new String[]{"text/*", "application/csv", "application/vnd.ms-excel"}));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    private void loadStudents() {
        AppExecutors.db().execute(() -> {
            String fromDate = DateUtils.last30DaysIso();
            List<com.example.ssjb.data.StudentAttendanceStat> stats = db.studentDao().getStudentStatsFromDate(fromDate);
            runOnUiThread(() -> {
                adapter.setItems(stats);
                adapter.filter(searchInput.getText() == null ? "" : searchInput.getText().toString());
            });
        });
    }

    private void importCsvFromUri(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }

        AppExecutors.db().execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, R.string.csv_import_failed, Toast.LENGTH_SHORT).show());
                    return;
                }
                List<Student> students = CsvImportUtil.parseStudents(new InputStreamReader(inputStream));
                if (students.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, R.string.csv_import_failed, Toast.LENGTH_SHORT).show());
                    return;
                }
                db.studentDao().insertAll(students);
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.csv_imported, students.size()), Toast.LENGTH_LONG).show();
                    loadStudents();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(this, ex.getMessage() == null ? getString(R.string.csv_import_failed) : ex.getMessage(), Toast.LENGTH_LONG).show());
            }
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
