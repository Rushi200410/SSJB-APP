package com.example.ssjb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssjb.R;
import com.example.ssjb.data.AppDatabase;
import com.example.ssjb.data.SessionHistoryRow;
import com.example.ssjb.util.AppExecutors;

import java.util.List;

public class SessionHistoryActivity extends AppCompatActivity {
    private AppDatabase db;
    private SessionHistoryAdapter adapter;
    private TextView emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);
        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.historyRecycler);
        emptyState = findViewById(R.id.historyEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionHistoryAdapter(row -> {
            Intent intent = new Intent(this, SessionAttendanceActivity.class);
            intent.putExtra("sessionId", row.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessions();
    }

    private void loadSessions() {
        AppExecutors.db().execute(() -> {
            List<SessionHistoryRow> sessions = db.sessionDao().getHistory();
            runOnUiThread(() -> {
                adapter.setItems(sessions);
                emptyState.setVisibility(sessions.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            });
        });
    }
}
