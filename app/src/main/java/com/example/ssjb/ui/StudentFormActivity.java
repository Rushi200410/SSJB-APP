package com.example.ssjb.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.Student;
import com.example.ssjb.util.AppExecutors;
import com.example.ssjb.util.DateUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class StudentFormActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "student_id";

    private AppDatabase db;
    private int studentId = -1;
    private EditText nameInput;
    private EditText middleNameInput;
    private EditText surnameInput;
    private EditText phoneInput;
    private Spinner instrumentSpinner;
    private EditText addressInput;
    private EditText joiningDateInput;
    private Spinner knowledgeSpinner;
    private EditText balanceInput;
    private TextView lastUpdatedText;
    private Student current;
    private String joiningDateIso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);

        db = AppDatabase.getInstance(this);
        studentId = getIntent().getIntExtra(EXTRA_STUDENT_ID, -1);
        nameInput = findViewById(R.id.studentNameInput);
        middleNameInput = findViewById(R.id.middleNameInput);
        surnameInput = findViewById(R.id.surnameInput);
        phoneInput = findViewById(R.id.phoneInput);
        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        addressInput = findViewById(R.id.addressInput);
        joiningDateInput = findViewById(R.id.joiningDateInput);
        knowledgeSpinner = findViewById(R.id.knowledgeSpinner);
        balanceInput = findViewById(R.id.balanceInput);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        Button saveButton = findViewById(R.id.saveStudentButton);

        ArrayAdapter<CharSequence> instrumentAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.instrument_options,
                android.R.layout.simple_spinner_item
        );
        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(instrumentAdapter);

        ArrayAdapter<CharSequence> knowledgeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.knowledge_options,
                android.R.layout.simple_spinner_item
        );
        knowledgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        knowledgeSpinner.setAdapter(knowledgeAdapter);

        joiningDateIso = DateUtils.todayIso();
        joiningDateInput.setText(DateUtils.formatDisplayDate(joiningDateIso));
        joiningDateInput.setOnClickListener(v -> showDatePicker());
        joiningDateInput.setFocusable(false);
        joiningDateInput.setLongClickable(false);

        saveButton.setOnClickListener(v -> save());
        if (studentId > 0) {
            load();
        } else {
            lastUpdatedText.setText(DateUtils.formatDisplayDateTime(DateUtils.nowIsoDateTime()));
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
                middleNameInput.setText(current.middleName);
                surnameInput.setText(current.surname);
                phoneInput.setText(current.phoneNumber);
                addressInput.setText(current.address);
                balanceInput.setText(current.balance == null ? "" : String.format(Locale.US, "%.2f", current.balance));
                joiningDateIso = current.joiningDateIso == null || current.joiningDateIso.trim().isEmpty()
                        ? DateUtils.todayIso()
                        : current.joiningDateIso;
                joiningDateInput.setText(DateUtils.formatDisplayDate(joiningDateIso));
                lastUpdatedText.setText(current.lastUpdatedIso == null || current.lastUpdatedIso.trim().isEmpty()
                        ? getString(R.string.unknown_value)
                        : DateUtils.formatDisplayDateTime(current.lastUpdatedIso));
                ArrayAdapter<CharSequence> instrumentAdapter = (ArrayAdapter<CharSequence>) instrumentSpinner.getAdapter();
                int instrumentPos = instrumentAdapter.getPosition(current.instrument);
                if (instrumentPos >= 0) {
                    instrumentSpinner.setSelection(instrumentPos);
                }
                ArrayAdapter<CharSequence> knowledgeAdapter = (ArrayAdapter<CharSequence>) knowledgeSpinner.getAdapter();
                int knowledgePos = knowledgeAdapter.getPosition(current.knowledgeLevel == null ? "" : current.knowledgeLevel);
                if (knowledgePos >= 0) {
                    knowledgeSpinner.setSelection(knowledgePos);
                }
            });
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            joiningDateIso = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            joiningDateInput.setText(DateUtils.formatDisplayDate(joiningDateIso));
        });
        picker.show(getSupportFragmentManager(), "student_joining_date");
    }

    private void save() {
        String name = valueOf(nameInput);
        String middleName = valueOf(middleNameInput);
        String surname = valueOf(surnameInput);
        String phone = valueOf(phoneInput);
        String instrument = instrumentSpinner.getSelectedItem().toString();
        String address = valueOf(addressInput);
        String knowledge = knowledgeSpinner.getSelectedItem().toString();
        String balanceText = valueOf(balanceInput);
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

        String lastUpdatedIso = DateUtils.nowIsoDateTime();
        String safeJoiningDate = joiningDateIso == null || joiningDateIso.trim().isEmpty()
                ? DateUtils.todayIso()
                : joiningDateIso;
        Student student = new Student(
                name,
                middleName,
                surname,
                phone,
                instrument,
                address,
                safeJoiningDate,
                knowledge,
                lastUpdatedIso,
                balance
        );

        AppExecutors.db().execute(() -> {
            if (studentId > 0 && current != null) {
                student.id = current.id;
                db.studentDao().update(student);
                runOnUiThread(this::finish);
            } else {
                long localId = db.studentDao().insert(student);
                student.id = (int) localId;
                pushStudentToFirebase(student);
            }
        });
    }

    private void pushStudentToFirebase(Student student) {
        DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("students");
        studentsRef.push().setValue(student)
                .addOnSuccessListener(unused -> runOnUiThread(this::finish))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Saved locally, but Firebase sync failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                }));
    }

    private String valueOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
