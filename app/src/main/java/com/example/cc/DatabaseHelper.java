package com.example.cc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="data.db";
    private static final String TABLE_NAME="data";
    private static final String CREATE_PING="create table ping(" +
            "_id integer primary key autoincrement," +
            "ping_run_time text," +
            "score text," +
            "sendPkgNum text, " +
            "receivePkgNum text, " +
            "packetLossNum text, " +
            "minRTT text, " +
            "maxRTT text, " +
            "meanRTT text," +
            "ipaddr text," +
            "ping_start_time text)";
    private static final String CREATE_TRACE="create table tracert(" +
            "_id integer primary key autoincrement," +
            "text text," +
            "ssid text," +
            "ipaddr text," +
            "time text)";
    private SQLiteDatabase db;
    public DatabaseHelper(Context context)
    {
        super(context,DB_NAME,null,2);
    }

//    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, name, factory, version);
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db=db;
        db.execSQL(CREATE_PING);
        db.execSQL(CREATE_TRACE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    public void insert(ContentValues values, String tableName){
        SQLiteDatabase db=getWritableDatabase();
        db.insert(tableName,null,values);
        db.close();
    }
    public void del(int id){
        if(null==db)
            db=getWritableDatabase();
        db.delete(TABLE_NAME,"_id=?",new String[]{String.valueOf(id)});
    }
    public Cursor query(String table_name){
        SQLiteDatabase db=getWritableDatabase();
        Cursor cursor=db.query(table_name,null,null,null,null,null,"_id desc");
        return cursor;
    }
    public Cursor queryById(String table_name, String id){
        SQLiteDatabase db=getWritableDatabase();
        Cursor cursor=db.query(table_name,null,"_id = ?", new String[]{id},null,null,"_id desc",null);
        return cursor;
    }
    public Cursor queryOne(String tableName){
        SQLiteDatabase db=getWritableDatabase();
        Cursor cursor=db.query(tableName,null,null,null,null,null,"_id desc", "1");
        return cursor;
    }
    public void close()
    {
        if(db!=null)
            db.close();
    }

}

