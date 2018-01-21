package com.sahilgaba.android.wakify;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sahilgaba on 4/22/17.
 */

public class AlarmListDbHelper extends SQLiteOpenHelper {

    private static AlarmListDbHelper mInstance = null;
    private static final String DB_NAME = "alarmslist.db";
    private static final int DB_VERSION = 1;

    private AlarmListDbHelper(Context context) {
        super(context,  DB_NAME, null, DB_VERSION);
    }

    public static AlarmListDbHelper getInstance(Context ctx) {

        if (mInstance == null) {
            mInstance = new AlarmListDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_ALARM_LIST_TABLE = "CREATE TABLE " + AlarmListContract.AlarmEntry.TABLE_NAME + " (" +
                AlarmListContract.AlarmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + AlarmListContract.AlarmEntry.ALARM_TIME_HR +
                " TEXT NOT NULL, " + AlarmListContract.AlarmEntry.ALARM_TIME_MIN + " TEXT NOT NULL, " +
                AlarmListContract.AlarmEntry.REPEAT + " TEXT NOT NULL, " + AlarmListContract.AlarmEntry.TOGGLE + " TEXT NOT NULL" + ");";
        db.execSQL(CREATE_ALARM_LIST_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AlarmListContract.AlarmEntry.TABLE_NAME);
        onCreate(db);
    }

}
