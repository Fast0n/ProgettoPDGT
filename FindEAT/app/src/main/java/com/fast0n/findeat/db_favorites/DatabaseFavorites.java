package com.fast0n.findeat.db_favorites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFavorites extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "id";
    public static final String TABLE_NAME = "records";
    public static final String COLUMN_RECORD = "favorite";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db_favorites";

    public DatabaseFavorites(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create records table
        String createTable = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_RECORD + " TEXT" + ")";
        db.execSQL(createTable);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertRecord(String record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        SQLiteDatabase db1 = this.getReadableDatabase();

        long id = 0;
        Cursor cursor = null;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_RECORD + "='" + record + "'";
        cursor = db1.rawQuery(sql, null);

        if (cursor.getCount() > 0) {

        } else {
            values.put(COLUMN_RECORD, record);
            id = db.insert(TABLE_NAME, null, values);
            db.close();

        }

        return id;

    }

    public Favorite getRecord(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_ID, COLUMN_RECORD }, COLUMN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare record object
        Favorite record = new Favorite(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_RECORD)));

        // close the db connection
        cursor.close();

        return record;
    }

    public List<Favorite> getAllRecords() {
        List<Favorite> records = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Favorite record = new Favorite();
                record.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                record.setRecord(cursor.getString(cursor.getColumnIndex(COLUMN_RECORD)));

                records.add(record);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return records list
        return records;
    }

    public int getRecordsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public int updateNote(Favorite record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_RECORD, record.getRecord());

        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(record.getId()) });
    }

    public void deleteRecord(Favorite record) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(record.getId()) });
        db.close();
    }
}