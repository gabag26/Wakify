package com.sahilgaba.android.wakify;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.content.SharedPreferences;


import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;


public class AlarmRunning extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    private static final String DEBUG_TAG = "SpotifyRunnning?";
    AudioManager mAudioManager;
    Context mContext;
    MusicPlayerStartTimerTask mMusicPlayerTask;
    Intent intentExtra;
    Vibrator shaker;
    Boolean vibrate;
    ImageButton imageButton;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private static SharedPreferences sharedPreferences;
    private static AlarmManager alarmManager;
    private  static SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        mContext = getApplicationContext();


        AlarmListDbHelper dbHelper = AlarmListDbHelper.getInstance(mContext);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        mDb = dbHelper.getWritableDatabase();

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);




        int result = mAudioManager.requestAudioFocus(AlarmRunning.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);



        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("AlarmRunning", "SLEEP");
                e.printStackTrace();
            }
            Log.e("RESULT", "FOCUS GRANTED");
            mMusicPlayerTask = new MusicPlayerStartTimerTask();
            mMusicPlayerTask.run();
        }


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        vibrate = sharedPreferences.getBoolean("vibrate", false);

        if (vibrate) {
            shaker = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 500, 500};
            shaker.vibrate(pattern, 0);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_alarm_running, null);


        setContentView(view);



        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


        Intent intent = getIntent();
        if(intent != null) {
            intentExtra = (Intent) intent.getExtras().get(Intent.EXTRA_INTENT);
        }

        imageButton = (ImageButton) view.findViewById(R.id.imageButton);

        imageButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));

        Intent intentForeground = new Intent(mContext, AlarmRunning.class);
        intentForeground.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intentForeground);

        final boolean last = isTaskRoot();

        imageButton.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight() {
                cancelAlarm(intentExtra);
                if (mAudioManager.isMusicActive()) playPauseMusic();
                if (vibrate) shaker.cancel();
                mAudioManager.abandonAudioFocus(AlarmRunning.this);

                if (last) finishAndRemoveTask ();
                else finish();

                return;
            }
            public void onSwipeLeft() {
                snoozeAlarm(intentExtra);
                if (mAudioManager.isMusicActive()) playPauseMusic();
                if (vibrate) shaker.cancel();
                mAudioManager.abandonAudioFocus(AlarmRunning.this);
                if (last) finishAndRemoveTask ();
                else finish();
                return;
            }
        });



    }

    @Override
    protected void onDestroy() {
        Log.e("ALarmRunning", "onDestroy called");
        super.onDestroy();
    }

    private void playPauseMusic() {
        int keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;

        if (!mAudioManager.isMusicActive() && !isSpotifyRunning()) {
            startMusicPlayer();
        }

        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setPackage("com.spotify.music");
        synchronized (this) {
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            mContext.sendOrderedBroadcast(i, null);
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            mContext.sendOrderedBroadcast(i, null);
        }
    }

    private void startMusicPlayer() {
        Log.e("AlarmRunning", "Starting Spotify");
        Intent startPlayer = new Intent(Intent.ACTION_MAIN);
        startPlayer.setPackage("com.spotify.music");
        startPlayer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startPlayer);

        /*if (mMusicPlayerStartTimer != null) {
            mMusicPlayerStartTimer.cancel();
        }

        mMusicPlayerStartTimer = new Timer("MusicPlayerStartTimer", true);
        mMusicPlayerStartTimer.schedule(new MusicPlayerStartTimerTask(), DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS);*/
    }

    private boolean isSpotifyRunning() {
        Process ps = null;
        try {
            String[] cmd = {
                    "sh",
                    "-c",
                    "ps | grep com.spotify.music"
            };

            ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            return ps.exitValue() == 0;
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not execute ps", e);
        } catch (InterruptedException e) {
            Log.e(DEBUG_TAG, "Could not execute ps", e);
        } finally {
            if (ps != null) {
                ps.destroy();
            }
        }

        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    private class MusicPlayerStartTimerTask extends TimerTask {
        @Override
        public void run() {
            if (!mAudioManager.isMusicActive())
            playPauseMusic();
            cancel();
        }
    }


    private void snoozeAlarm(Intent intentExtra) {
        int requestCode = intentExtra.getIntExtra("RequestCode", 0);
        PendingIntent pendingIntentExtra = PendingIntent.getBroadcast(mContext, requestCode, intentExtra, PendingIntent.FLAG_UPDATE_CURRENT);
        int snoozeTime = Integer.valueOf(sharedPreferences.getString("snooze_time", "2"));
        Log.e("SnoozeTime", Integer.toString(snoozeTime));
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (1000 * snoozeTime * 60), pendingIntentExtra);
    }

    public static void cancelAlarm(Intent intentExtra) {
        Log.e("AlarmRunning", "CancelAlarm");
        int requestCode = intentExtra.getIntExtra("RequestCode", 0);
        String idS = Integer.toString(requestCode);
        String repeat = intentExtra.getStringExtra("repeat");

        if (repeat.equals("never")) {

            Cursor cursor = mDb.rawQuery("SELECT * FROM " + AlarmListContract.AlarmEntry.TABLE_NAME + " WHERE " + AlarmListContract.AlarmEntry._ID + " = " + idS, null);
            String hr = "";
            String min = "";

            if (cursor.moveToFirst()) {
                hr = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_HR));
                min = cursor.getString(cursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_MIN));
            }


            ContentValues cv = new ContentValues();

            cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_HR, hr);
            cv.put(AlarmListContract.AlarmEntry.ALARM_TIME_MIN, min);
            cv.put(AlarmListContract.AlarmEntry.REPEAT, repeat);
            cv.put(AlarmListContract.AlarmEntry.TOGGLE, "OFF");

            mDb.update(AlarmListContract.AlarmEntry.TABLE_NAME, cv, AlarmListContract.AlarmEntry._ID + "=" + idS, null);
        }
    }


}
