package com.sahilgaba.android.wakify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by sahilgaba on 7/25/17.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    private Context mContext;
    private AlarmManager alarmManager;
    SQLiteDatabase mDb;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BOOTING DONE", "NOW");

        mContext = context;
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        AlarmListDbHelper dbHelper = AlarmListDbHelper.getInstance(context);
        mDb = dbHelper.getWritableDatabase();

        bootAlarms();
    }

    public void bootAlarms() {
        Log.e("Boot Alarms", "BOOTING");
        Cursor cursor = getAllAlarms();
        int hr;
        int min;
        String repeat;
        int requestCode;
        Long id;
        try {
            while (cursor.moveToNext()) {
                Calendar calender = Calendar.getInstance();
                hr = Integer.valueOf(cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_HR)));
                min = Integer.valueOf(cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_MIN)));
                repeat = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
                id = cursor.getLong(cursor.getColumnIndex(AlarmListContract.AlarmEntry._ID));
                calender.set(Calendar.HOUR_OF_DAY, hr);
                calender.set(Calendar.MINUTE, min);
                requestCode = (int) (id % Integer.MAX_VALUE);
                Intent intentNew = new Intent(mContext, AlarmReciever.class);
                intentNew.putExtra("RequestCode", requestCode);
                intentNew.putExtra("repeat", repeat);
                PendingIntent pendingIntentNew = PendingIntent.getBroadcast(mContext, requestCode, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);
                if (repeat.equals("never")) alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntentNew);
                else alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 24 * 60 * 60, pendingIntentNew);
            }
        } finally {
            cursor.close();
        }
    }

    private Cursor getAllAlarms(){
        return mDb.query(AlarmListContract.AlarmEntry.TABLE_NAME, null, null, null, null, null, AlarmListContract.AlarmEntry._ID);
    }
}
