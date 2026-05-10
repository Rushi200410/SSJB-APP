package com.example.ssjb.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.Student;
import com.example.ssjb.util.AppExecutors;

public class StudentFormActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "student_id";

    private AppDatabase db;
    private int studentId = -1;
    private EditText nameInput;
    private Spinner instrumentSpinner;
    private EditText balanceInput;
    private Student current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);

        db = AppDatabase.getInstance(this);
        studentId = getIntent().getIntExtra(EXTRA_STUDENT_ID, -1);
        nameInput = findViewById(R.id.studentNameInput);
        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        balanceInput = findViewById(R.id.balanceInput);
        Button saveButton = findViewById(R.id.saveStudentButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.instrument_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> save());
        if (studentId > 0) {
            load();
        }
    }

    private void load() {
        AppExecutors.db().execute(() -> {
            current = db.studentDao().getById(studentId);
            runOnUiThread(() -> {
                if (current == null) {
                    finish();
                    return;
                }
                nameInput.setText(current.name);
                balanceInput.setText(current.balance == null ? "" : String.valueOf(current.balance));
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) instrumentSpinner.getAdapter();
                int pos = adapter.getPosition(current.instrument);
                if (pos >= 0) {
                    instrumentSpinner.setSelection(pos);
                }
            });
        });
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String instrument = instrumentSpinner.getSelectedItem().toString();
        String balanceText = balanceInput.getText().toString().trim();
        Double balance;
        try {
            balance = balanceText.isEmpty() ? null : Double.parseDouble(balanceText);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, R.string.invalid_balance, Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            return;
        }
        AppExecutors.db().execute(() -> {
            if (studentId > 0 && current != null) {
                current.name = name;
                current.instrument = instrument;
                current.balance = balance;
                db.studentDao().update(current);
            } else {
                db.studentDao().insert(new Student(name, instrument, balance));
            }
            runOnUiThread(this::finish);
        });
    }
}
