package com.mertcaliskanyurek.sensormonitor.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "HealthMonitor.db";

    private static final String TABLE_ACTIVITY_CREATE =
            "CREATE TABLE " + DatabaseContract.ACTIVITY_TABLE_NAME + " (" +
                    DatabaseContract.ACTIVITY_ENTRY.COLMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.ACTIVITY_ENTRY.COLMN_SENSOR_TYPE + " INTEGER," +
                    DatabaseContract.ACTIVITY_ENTRY.COLMN_VALUE + " INTEGER," +
                    DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME + " INTEGER)";

    public DatabaseHelper(Context cx) {
        super(cx,DATABASE_NAME,null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_ACTIVITY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ACTIVITY_TABLE_NAME);
        onCreate(db);
    }

    public void insertActivity(int sensorType,float value)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ACTIVITY_ENTRY.COLMN_SENSOR_TYPE,sensorType);
        values.put(DatabaseContract.ACTIVITY_ENTRY.COLMN_VALUE,value);
        values.put(DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME, Calendar.getInstance().getTimeInMillis());

        getWritableDatabase().insert(DatabaseContract.ACTIVITY_TABLE_NAME,null,values);
    }

    public Map<Long,Integer> getActivity(int sensorType)
    {
        SQLiteDatabase db = getReadableDatabase();

        String selection = DatabaseContract.ACTIVITY_ENTRY.COLMN_SENSOR_TYPE + " = ? ";
        String[] selArgs = {String.valueOf(sensorType)};

        Cursor cursor;
        Map<Long,Integer> values;
        Long tempTime;
        Integer tempValue;

        String orderBy = DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME + " ASC";

        cursor = db.query(DatabaseContract.ACTIVITY_TABLE_NAME,null,selection,
                    selArgs,null,null,orderBy);

        values = new LinkedHashMap<>(cursor.getCount());

        while (cursor.moveToNext()) {
            tempTime = cursor.getLong(cursor.getColumnIndex(DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME));
            tempValue = cursor.getInt(cursor.getColumnIndex(DatabaseContract.ACTIVITY_ENTRY.COLMN_VALUE));
            values.put(tempTime, tempValue);
        }

        cursor.close();
        return values;
    }

    public Map<Long,Integer> getActivity(int sensorType, long startTime, long finTime)
    {
        SQLiteDatabase db = getReadableDatabase();
        String selection = DatabaseContract.ACTIVITY_ENTRY.COLMN_SENSOR_TYPE + " = ? AND " +
                                    DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME + " BETWEEN ? AND ?";
        String[] selArgs = {String.valueOf(sensorType),String.valueOf(startTime),String.valueOf(finTime)};
        String orderBy = DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME + " ASC";

        Cursor cursor;
        Map<Long,Integer> values;
        Long tempTime;
        Integer tempValue;

        cursor = db.query(DatabaseContract.ACTIVITY_TABLE_NAME,null,selection,
                selArgs,null,null,orderBy);

        values = new LinkedHashMap<>(cursor.getCount());

        while (cursor.moveToNext()) {
            tempTime = cursor.getLong(cursor.getColumnIndex(DatabaseContract.ACTIVITY_ENTRY.COLMN_TIME));
            tempValue = cursor.getInt(cursor.getColumnIndex(DatabaseContract.ACTIVITY_ENTRY.COLMN_VALUE));
            values.put(tempTime, tempValue);
        }

        cursor.close();
        return values;
    }

}
