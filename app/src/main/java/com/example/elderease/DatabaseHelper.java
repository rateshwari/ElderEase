package com.example.elderease;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "elderease.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "medicine_reminders";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medicine_name TEXT, " +
                "type TEXT, " +
                "frequency TEXT, " +
                "date TEXT, " +
                "time TEXT, " +
                "repeat TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertReminder(String name, String type, String freq, String date, String time, String repeat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("medicine_name", name);
        cv.put("type", type);
        cv.put("frequency", freq);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("repeat", repeat);

        long result = db.insert(TABLE_NAME, null, cv);
        return result != -1;
    }

    public ArrayList<HashMap<String, String>> getAllReminders() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        while (cursor.moveToNext()) {
            HashMap<String, String> reminder = new HashMap<>();
            reminder.put("medicine_name", cursor.getString(1));
            reminder.put("type", cursor.getString(2));
            reminder.put("frequency", cursor.getString(3));
            reminder.put("date", cursor.getString(4));
            reminder.put("time", cursor.getString(5));
            reminder.put("repeat", cursor.getString(6));
            list.add(reminder);
        }
        cursor.close();
        return list;
    }
}
