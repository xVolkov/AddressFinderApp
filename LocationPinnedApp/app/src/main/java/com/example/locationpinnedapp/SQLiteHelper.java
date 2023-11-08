package com.example.locationpinnedapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "location_database";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "location";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    // Class constructor
    public SQLiteHelper(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override // DB onCreate default method logic - setting up the table in DB
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(COLUMN_ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(COLUMN_ADDRESS)
                .append(" TEXT, ")
                .append(COLUMN_LATITUDE)
                .append(" REAL, ")
                .append(COLUMN_LONGITUDE)
                .append(" REAL)");
        db.execSQL(sql.toString());} // converting to string

    @Override // DB onUpgrade default method logic - on table upgrade, do..
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}// Handle database schema upgrades if needed

    // adding data to DB
    public long addData(SQLiteDatabase db, String address, double lat, double longt) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_LATITUDE, lat);
        cv.put(COLUMN_LONGITUDE, longt);
        return db.insert(TABLE_NAME, null, cv);}

    // deleting data in DB
    public boolean deleteData(SQLiteDatabase db, int dataID) {
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(dataID) };
        int rows = db.delete(TABLE_NAME, whereClause, whereArgs);
        return rows > 0;} // checks if 1 row (at least) is deleted, then returns true

    // editing the database
    public int updateData(SQLiteDatabase db, int dataID, String address, double lat, double longt) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_LATITUDE, lat);
        cv.put(COLUMN_LONGITUDE, longt);
        String where = COLUMN_ID + " = ?"; // where clause to lookup data
        String[] whereArgs = {String.valueOf(dataID)}; // looking up dataID
        return db.update(TABLE_NAME, cv, where, whereArgs);} // updating the data in the DB
}
