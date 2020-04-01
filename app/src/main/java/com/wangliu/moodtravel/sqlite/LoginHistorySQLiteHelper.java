package com.wangliu.moodtravel.sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class LoginHistorySQLiteHelper extends SQLiteOpenHelper {

    public String tableName = "loginHistory";

    public LoginHistorySQLiteHelper(@Nullable Context context) {
        //这个库名要和表名一样，不知道什么鬼设定
        super(context, "loginHistory", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ tableName +"(id Integer primary key autoincrement, username varchar(20) not null, password varchar(20) not null)");
    }

    @SuppressLint("Recycle")
    public void insertData(String username, String password, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select password from " + tableName + " where username = '" + username +"'", null);
        if (cursor != null && cursor.getCount() > 0) {
            //这里要先让游标到first也就是0，因为游标是从-1开始的，不知道什么鬼设定
            cursor.moveToFirst();
            if (!cursor.getString(cursor.getColumnIndex("password")).equals(password)) {
                db.execSQL("update password set password = '" + password + "' from " + tableName);
            }
            return;
        }

        db.execSQL("insert into " + tableName + "(username, password) values(?, ?)", new String[]{username, password});

        cursor = db.rawQuery("select id from " + tableName, null);
        if (cursor.getCount() > 15) {
            db.execSQL("delete from " + tableName + " order by id limit 1");
        }
        db.close();
    }

    public void initTable(SQLiteDatabase db) {
        db.execSQL("delete from " + tableName);
        db.execSQL("update sqlite_sequence set seq=0 where name = '" + tableName + "'");
        db.close();
    }

    public void deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " +tableName + " where id = " + id);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
