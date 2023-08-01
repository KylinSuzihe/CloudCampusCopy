package com.example.cc;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class PingDetails extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details_ping);
        ListView listView = findViewById(R.id.details_ping);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        final DatabaseHelper dbHelper=new DatabaseHelper(this);
        Cursor cursor=dbHelper.queryById("ping", id);
        String[] from={"ipaddr", "ping_start_time", "ping_run_time","score","sendPkgNum", "receivePkgNum", "packetLossNum", "minRTT", "maxRTT", "meanRTT"};
        int[] to={R.id.destination, R.id.ping_start_time, R.id.ping_run_time, R.id.score, R.id.sendPkgNum, R.id.receivePkgNum, R.id.packetLossNum, R.id.minRTT, R.id.maxRTT, R.id.meanRTT};
        SimpleCursorAdapter scadapter=new SimpleCursorAdapter(this,R.layout.ping_statistic,cursor,from,to);
        listView.setAdapter(scadapter);

    }
}
