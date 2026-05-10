package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.R;
import com.example.ssjb.util.DateUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SessionFormActivity extends AppCompatActivity {
    public static final String EXTRA_SESSION_TYPE = "session_type";

    private String sessionType;
    private EditText dateInput;
    private EditText dayInput;
    private EditText timeInput;
    private Spinner classModeSpinner;
    private EditText callTitleInput;
    private EditText callDescriptionInput;
    private EditText callLocationInput;
    private String selectedDateIso;
    private String selectedTimeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_form);

        sessionType = getIntent().getStringExtra(EXTRA_SESSION_TYPE);
        if (sessionType == null) {
            sessionType = "CLASS";
        }
        TextView title = findViewById(R.id.sessionFormTitle);
        dateInput = findViewById(R.id.sessionDateInput);
        dayInput = findViewById(R.id.sessionDayInput);
        timeInput = findViewById(R.id.sessionTimeInput);
        classModeSpinner = findViewById(R.id.classModeSpinner);
        callTitleInput = findViewById(R.id.callTitleInput);
        callDescriptionInput = findViewById(R.id.callDescriptionInput);
        callLocationInput = findViewById(R.id.callLocationInput);
        Button continueButton = findViewById(R.id.continueAttendanceButton);

        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                this, R.array.class_mode_options, android.R.layout.simple_spinner_item
        );
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classModeSpinner.setAdapter(classAdapter);

        boolean isClass = "CLASS".equals(sessionType);
        title.setText(isClass ? R.string.class_details : R.string.call_details);
        classModeSpinner.setVisibility(isClass ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.callFieldsContainer).setVisibility(isClass ? android.view.View.GONE : android.view.View.VISIBLE);

        selectedDateIso = DateUtils.todayIso();
        selectedTimeLabel = DateUtils.formatDisplayTime(DateUtils.nowIsoDateTime());
        updateDateViews();
        updateTimeView();

        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setOnClickListener(v -> showTimePicker());
        dateInput.setFocusable(false);
        timeInput.setFocusable(false);

        continueButton.setOnClickListener(v -> goNext());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            updateDateViews();
        });
        picker.show(getSupportFragmentManager(), "session_date");
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTitleText(R.string.time_hint)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
            calendar.set(Calendar.MINUTE, picker.getMinute());
            selectedTimeLabel = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime());
            updateTimeView();
        });
        picker.show(getSupportFragmentManager(), "session_time");
    }

    private void updateDateViews() {
        dateInput.setText(DateUtils.formatDisplayDate(selectedDateIso));
        dayInput.setText(DateUtils.dayLabelForIsoDate(selectedDateIso));
    }

    private void updateTimeView() {
        timeInput.setText(selectedTimeLabel);
    }

    private void goNext() {
        if (selectedDateIso == null || selectedDateIso.trim().isEmpty() ||
                selectedTimeLabel == null || selectedTimeLabel.trim().isEmpty()) {
            Toast.makeText(this, R.string.fill_required, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, SessionAttendanceActivity.class);
        i.putExtra(EXTRA_SESSION_TYPE, sessionType);
        i.putExtra("date", selectedDateIso);
        i.putExtra("day", dayInput.getText().toString().trim());
        i.putExtra("time", selectedTimeLabel);
        i.putExtra("classMode", classModeSpinner.getSelectedItem().toString());
        i.putExtra("callTitle", callTitleInput.getText().toString().trim());
        i.putExtra("callDescription", callDescriptionInput.getText().toString().trim());
        i.putExtra("callLocation", callLocationInput.getText().toString().trim());
        startActivity(i);
    }
}
