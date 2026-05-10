package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.R;

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
        classModeSpinner.setVisibility(isClass ? View.VISIBLE : View.GONE);
        findViewById(R.id.callFieldsContainer).setVisibility(isClass ? View.GONE : View.VISIBLE);

        continueButton.setOnClickListener(v -> goNext());
    }

    private void goNext() {
        if (dateInput.getText().toString().trim().isEmpty() ||
                dayInput.getText().toString().trim().isEmpty() ||
                timeInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.fill_required, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, SessionAttendanceActivity.class);
        i.putExtra(EXTRA_SESSION_TYPE, sessionType);
        i.putExtra("date", dateInput.getText().toString().trim());
        i.putExtra("day", dayInput.getText().toString().trim());
        i.putExtra("time", timeInput.getText().toString().trim());
        i.putExtra("classMode", classModeSpinner.getSelectedItem().toString());
        i.putExtra("callTitle", callTitleInput.getText().toString().trim());
        i.putExtra("callDescription", callDescriptionInput.getText().toString().trim());
        i.putExtra("callLocation", callLocationInput.getText().toString().trim());
        startActivity(i);
    }
}
