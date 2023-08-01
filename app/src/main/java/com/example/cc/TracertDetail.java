package com.example.cc;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class TracertDetail extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details_tracert);
        ListView listView = findViewById(R.id.details_tracert);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        final DatabaseHelper dbHelper=new DatabaseHelper(this);
        Cursor cursor=dbHelper.queryById("tracert", id);
        String[] from={"ipaddr", "time", "text", "ssid"};
        int[] to={R.id.ipaddr, R.id.time, R.id.content, R.id.ssid};
        SimpleCursorAdapter scadapter=new SimpleCursorAdapter(this,R.layout.tracert_statistic,cursor,from,to);
        listView.setAdapter(scadapter);

    }
}
