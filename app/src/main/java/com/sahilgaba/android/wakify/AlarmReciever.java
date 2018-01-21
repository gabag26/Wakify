package com.sahilgaba.android.wakify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

import android.media.AudioManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by sahilgaba on 6/3/17.
 */

public class AlarmReciever extends BroadcastReceiver  {

    private static final String DEBUG_TAG = "SpotifyRunnning?";

    @Override
    public void onReceive(Context context, Intent intent)  {


        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if(intent.getStringExtra("repeat").equals("weekdays") && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
            return;
        }
        if(intent.getStringExtra("repeat").equals("weekends") && day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
            return;
        }

        if (!isSpotifyRunning()) {
            startMusicPlayer(context);

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Log.e("AlarmReciever", "SLEEP");
                e.printStackTrace();
            }
        }

        Log.e("BR", "intent recived");
        Intent intentScreen = new Intent(context, AlarmRunning.class);
        intentScreen.putExtra(Intent.EXTRA_INTENT, intent);
        intentScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intentScreen);
        return;
    }

    private void startMusicPlayer(Context mContext) {
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

}
