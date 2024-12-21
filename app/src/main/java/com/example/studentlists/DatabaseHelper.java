package com.example.studentlists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "students.db";
    private static final String TABLE_NAME = "students";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "NAME";
    private static final String COL_3 = "AGE";
    private static final String COL_4 = "BIRTHDAY";
    private static final String COL_5 = "SEX";
    private static final String COL_6 = "EMAIL";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, AGE INTEGER, BIRTHDAY TEXT, SEX TEXT, EMAIL TEXT)");
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Inserts a new student record into the database
    public boolean insertData(String name, int age, String birthday, String sex, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String normalizedName = name.trim().replaceAll("\\s+", " ").toUpperCase();
        contentValues.put(COL_2, normalizedName);
        contentValues.put(COL_3, age);
        contentValues.put(COL_4, birthday);
        contentValues.put(COL_5, sex);
        contentValues.put(COL_6, email);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            Log.e("DatabaseHelper", "Data insertion failed");
        }
        return result != -1;
    }

    // Retrieves all student records from the database
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // Deletes a student record from the database by name
    public Integer deleteData(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String normalizedName = name.trim().replaceAll("\\s+", " ").toUpperCase();
        return db.delete(TABLE_NAME, "UPPER(NAME) = ?", new String[]{normalizedName});
    }
    
    // Checks if an email already exists in the database
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE EMAIL = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}