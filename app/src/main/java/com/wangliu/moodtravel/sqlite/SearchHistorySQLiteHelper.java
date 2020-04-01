package com.wangliu.moodtravel.sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SearchHistorySQLiteHelper extends SQLiteOpenHelper {

    /**
     * 构造方法
     * @param context   上下文
     * @param name      数据库名称
     * @param factory   游标工厂（一般填null）
     * @param version   数据库版本
     */
    public SearchHistorySQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table history(hno Integer primary key autoincrement, record varchar(200))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void delete(SQLiteDatabase db) {
        db.execSQL("delete from history");
        db.execSQL("update sqlite_sequence set seq=0 where name = 'history'");
        db.close();
    }

    public void insertData(SQLiteDatabase db, String word) {
        db.execSQL("insert into history(record) values('"+ word +"')");
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select hno from history order by hno asc", null);
        if (cursor.getCount() > 4) {
            db.execSQL("delete from history where hno = (select hno from history limit 1)");
        }
        db.close();
    }

    public boolean hasData(SQLiteDatabase db) {
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select hno from history where hno > 0", null);
        return cursor.moveToNext();
    }

    public boolean hasNoRecord(SQLiteDatabase db, String word) {
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("select record from history where record = ?", new String[]{word});
        return !cursor.moveToNext();
    }
}
