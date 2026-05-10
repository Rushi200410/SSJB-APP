package com.example.ssjb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ssjb.ui.SessionFormActivity;
import com.example.ssjb.ui.SessionHistoryActivity;
import com.example.ssjb.ui.StudentsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button studentsButton = findViewById(R.id.studentsButton);
        Button classButton = findViewById(R.id.classButton);
        Button callButton = findViewById(R.id.callButton);
        Button historyButton = findViewById(R.id.historyButton);

        studentsButton.setOnClickListener(v -> startActivity(new Intent(this, StudentsActivity.class)));
        classButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SessionFormActivity.class);
            intent.putExtra(SessionFormActivity.EXTRA_SESSION_TYPE, "CLASS");
            startActivity(intent);
        });
        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SessionFormActivity.class);
            intent.putExtra(SessionFormActivity.EXTRA_SESSION_TYPE, "CALL");
            startActivity(intent);
        });
        historyButton.setOnClickListener(v -> startActivity(new Intent(this, SessionHistoryActivity.class)));
    }
}
