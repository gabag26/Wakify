package com.sahilgaba.android.wakify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private RecyclerView alarmRecyclerView;
    private static AlarmListAdapter alarmAdapter;
    private RecyclerView.LayoutManager alarmLayoutManager;
    private  FloatingActionButton addAlarmFloatingButton;
    private  static SQLiteDatabase mDb;
    private static HashMap<Long, PendingIntent> mapIntent;
    private static HashMap<Integer, Long> mapLong;
    private static int intentCount = 0;
    private static AlarmManager alarmManager;
    private static Context mContext;
    private static int snoozeTime = 2;
    private static SharedPreferences sharedPreferences;
    private static final String ALARM_COUNT = "AlarmCount";
    private static FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;



        if (isFirstTime()){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setMessage("Please ensure that Spotify is running at the paused state from where you want your alarm to start. " +
                    "Alarm would run at the volume you have set for media. Swipe left to snooze alarm and right to switch it off.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setCancelable(false).show();
        }


        AlarmListDbHelper dbHelper = AlarmListDbHelper.getInstance(mContext);
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = getAllAlarms();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        mapIntent = new HashMap<>();
        mapLong = new HashMap<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        alarmRecyclerView = (RecyclerView) findViewById(R.id.alarms_recycler_view);
        alarmRecyclerView.setHasFixedSize(true);
        alarmLayoutManager = new LinearLayoutManager(this);
        alarmRecyclerView.setLayoutManager(alarmLayoutManager);
        alarmAdapter = new AlarmListAdapter(this, cursor);

        alarmRecyclerView.setAdapter(alarmAdapter);
        fm = getSupportFragmentManager();

        addAlarmFloatingButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        addAlarmFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmDialogFragment alarmDialogFragment = new AlarmDialogFragment();
                alarmDialogFragment.show(fm, "Alarm Dialog");
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                removeAlarm(id);
                alarmAdapter.updateRecycler(getAllAlarms());
            }
        }).attachToRecyclerView(alarmRecyclerView);

    }

    @Override
    protected void onStart() {
        AlarmListDbHelper dbHelper = AlarmListDbHelper.getInstance(mContext);
        mDb = dbHelper.getWritableDatabase();
        super.onStart();
    }

    public static long addAlarm(java.util.Calendar calender, String repeat) {
        String hr = Integer.toString(calender.get(Calendar.HOUR_OF_DAY));
        String min = Integer.toString(calender.get(Calendar.MINUTE));
        Log.e("REPEAT ADD", repeat);
        Log.e("ADD ALARM HR", hr);


        Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry.ALARM_TIME_HR + " = " + hr + " AND " + AlarmListContract.AlarmEntry.ALARM_TIME_MIN + " = " + min, null);
        if (cursor.moveToFirst()){
            long id = cursor.getLong(cursor.getColumnIndex(AlarmListContract.AlarmEntry._ID));
            updateAlarm(calender, Long.toString(id), repeat);
            return id;
        }
        ContentValues cv = new ContentValues();
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_HR, hr);
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_MIN, min);
        cv.put(AlarmListContract.AlarmEntry.REPEAT, repeat);
        cv.put(AlarmListContract.AlarmEntry.TOGGLE, "ON");
        long insertLong = mDb.insert(AlarmListContract.AlarmEntry.TABLE_NAME, null, cv);
        Log.e("ID add", Long.toString(insertLong));
        alarmAdapter.updateRecycler(getAllAlarms());


        intentCount = (int) (insertLong % Integer.MAX_VALUE);

        Log.e("RequestCode", Integer.toString(intentCount));

        Calendar calenderNow = Calendar.getInstance();
        calenderNow.setTimeInMillis(System.currentTimeMillis());

        if(calenderNow.getTimeInMillis() - calender.getTimeInMillis() > 60000) calender.add(Calendar.DATE, 1);

        //Adding intent
        Intent intent = new Intent(mContext, AlarmReciever.class);
        intent.putExtra("RequestCode", intentCount);
        intent.putExtra("repeat", repeat);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, intentCount , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (repeat.equals("never")) alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
        else alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 24 * 60 * 60, pendingIntent);
        return insertLong;
    }



    private static Cursor getAllAlarms(){
        return mDb.query(AlarmListContract.AlarmEntry.TABLE_NAME, null, null, null, null, null, "CAST(" + AlarmListContract.AlarmEntry.ALARM_TIME_HR + " AS INTEGER)" + " ASC, " + "CAST(" + AlarmListContract.AlarmEntry.ALARM_TIME_MIN + " AS INTEGER)" + " ASC");
    }



    private static boolean removeAlarm(long id) {
        int requestCode = (int) (id % Integer.MAX_VALUE);
        String idS = Long.toString(id);
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry._ID + " = " + idS, null);
        String repeat = "never";
        if (cursor.moveToFirst()) {
            repeat = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
        }
        Intent intent = new Intent(mContext, AlarmReciever.class);
        intent.putExtra("RequestCode", requestCode);
        intent.putExtra("repeat", repeat);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        return mDb.delete(AlarmListContract.AlarmEntry.TABLE_NAME, AlarmListContract.AlarmEntry._ID + "=" + idS, null) > 0;
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.actions_settings) {
           Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
           startActivity(startSettingsActivity);
            return true;
        }
        if(id == R.id.actions_help) {
            Intent startHelpActivity = new Intent(this, Help.class);
            startActivity(startHelpActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void editListItem(long id) {
        Log.e("ID", Long.toString(id));
        String idS = Long.toString(id);
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry._ID + " = " + idS, null);
        String hr = "";
        String min = "";
        String repeat = "never";
        if (cursor.moveToFirst()) {
            hr = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_HR));
            min = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_MIN));
            repeat = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
        }
        Log.e("REPEAT IN EDITLIST", repeat);
        ListItemDialogFragment newListItemDialogFragment = ListItemDialogFragment.newInstance(idS, hr, min, repeat);
        newListItemDialogFragment.show(fm, "On Click Dialog");
        cursor.close();
    }


    public static void updateAlarm(Calendar calendar, String id, String repeat) {
        String repeatOld = "never";
        long idLong = Long.valueOf(id);
        int requestCode = (int) (idLong % Integer.MAX_VALUE);
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry._ID + " = " + id, null);
        if (cursor.moveToFirst()) {
            repeatOld = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
        }
        Intent intent = new Intent(mContext, AlarmReciever.class);
        intent.putExtra("RequestCode", requestCode);
        intent.putExtra("repeat", repeatOld);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        String hr = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String min = Integer.toString(calendar.get(Calendar.MINUTE));
        Log.e("REPEAT UPDATE", repeat);
        ContentValues cv = new ContentValues();
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_HR, hr);
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_MIN, min);
        cv.put(AlarmListContract.AlarmEntry.REPEAT, repeat);
        cv.put(AlarmListContract.AlarmEntry.TOGGLE, "ON");
        mDb.update(AlarmListContract.AlarmEntry.TABLE_NAME, cv, AlarmListContract.AlarmEntry._ID + "=" + id, null);
        alarmAdapter.updateRecycler(getAllAlarms());

        Calendar calenderNow = Calendar.getInstance();
        calenderNow.setTimeInMillis(System.currentTimeMillis());

        if(calendar.before(calenderNow)) calendar.add(Calendar.DATE, 1);

        Intent intentNew = new Intent(mContext, AlarmReciever.class);
        intentNew.putExtra("RequestCode", requestCode);
        intentNew.putExtra("repeat", repeat);
        PendingIntent pendingIntentNew = PendingIntent.getBroadcast(mContext, requestCode, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);
        if (repeat.equals("never"))alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntentNew);
        else alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 24 * 60 * 60, pendingIntentNew);

    }


    public static void switchToggle(long id) {
        String idS = Long.toString(id);
        int requestCode = (int) (id % Integer.MAX_VALUE);
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry._ID + " = " + idS, null);
        String hr = "";
        String min = "";
        String repeat = "never";
        String toggleOnOff = "";
        Calendar calender = Calendar.getInstance();

        if (cursor.moveToFirst()) {
            hr = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_HR));
            min = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_MIN));
            repeat = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
            toggleOnOff = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.TOGGLE));
        }

        calender.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hr));
        calender.set(Calendar.MINUTE, Integer.valueOf(min));

        Calendar calenderNow = Calendar.getInstance();
        calenderNow.setTimeInMillis(System.currentTimeMillis());

        if(calender.before(calenderNow)) calender.add(Calendar.DATE, 1);

        Intent intentNew = new Intent(mContext, AlarmReciever.class);
        intentNew.putExtra("RequestCode", requestCode);
        intentNew.putExtra("repeat", repeat);
        PendingIntent pendingIntentNew = PendingIntent.getBroadcast(mContext, requestCode, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);

        if (toggleOnOff.equals("ON")) alarmManager.cancel(pendingIntentNew);
        else {
            if (repeat.equals("never"))alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntentNew);
            else alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 24 * 60 * 60, pendingIntentNew);
        }

        ContentValues cv = new ContentValues();
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_HR, hr);
        cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_MIN, min);
        cv.put(AlarmListContract.AlarmEntry.REPEAT, repeat);
        if (toggleOnOff.equals("OFF")) cv.put(AlarmListContract.AlarmEntry.TOGGLE, "ON");
        else cv.put(AlarmListContract.AlarmEntry.TOGGLE, "OFF");
        mDb.update(AlarmListContract.AlarmEntry.TABLE_NAME, cv, AlarmListContract.AlarmEntry._ID + "=" + id, null);
        alarmAdapter.updateRecycler(getAllAlarms());

        cursor.close();

    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore13", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore13", true);
            editor.apply();
        }
        return !ranBefore;
    }


    public static void updateView() {
        Log.e("MainActivity", "UpdatingView");
        alarmAdapter.updateRecycler(getAllAlarms());
    }



    @Override
    protected void onResume() {
        alarmAdapter.updateRecycler(getAllAlarms());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.e("MainActivity", "OnDestry");
        super.onDestroy();
    }
}
