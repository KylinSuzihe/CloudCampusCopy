package com.example.cc;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class HistoryActivity extends AppCompatActivity {
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.history_ping_list);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (type.equals("ping")) {
            getPings();
        }
        else {
            getTracert();
        }

    }

    private void getTracert() {
        final DatabaseHelper dbHelper=new DatabaseHelper(this);
        Cursor cursor=dbHelper.query("tracert");
        String[] from={"time","ssid","ipaddr"};
        int[] to={R.id.startTime, R.id.ssid, R.id.ipaddr};
        SimpleCursorAdapter scadapter=new SimpleCursorAdapter(this,R.layout.history_tracert_statistic,cursor,from,to);
        listView.setAdapter(scadapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final long temp=id;
            Intent intent = new Intent(HistoryActivity.this, TracertDetail.class);
            intent.putExtra("id", String.valueOf(temp));
            startActivity(intent);
        });
        dbHelper.close();
    }

    private void getPings() {
        final DatabaseHelper dbHelper=new DatabaseHelper(this);
        Cursor cursor=dbHelper.query("ping");
        String[] from={"ipaddr","packetLossNum","ping_run_time","ping_start_time"};
        int[] to={R.id.ipaddr, R.id.lostRate, R.id.usedTime, R.id.startTime};
        SimpleCursorAdapter scadapter=new SimpleCursorAdapter(this,R.layout.history_ping_statistic,cursor,from,to);
        listView.setAdapter(scadapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final long temp=id;
            Intent intent = new Intent(HistoryActivity.this, PingDetails.class);
            intent.putExtra("id", String.valueOf(temp));
            startActivity(intent);
        });
        dbHelper.close();
    }
}
