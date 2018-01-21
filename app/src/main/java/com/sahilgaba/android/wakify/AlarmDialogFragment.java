package com.sahilgaba.android.wakify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import java.util.Calendar;

/**
 * Created by sahilgaba on 4/19/17.
 */

public class AlarmDialogFragment extends DialogFragment {

    EditText hrEt;
    EditText minEt;
    public static final String HOUR = "hour";
    public static final String MIN = "min";
    private Button addButton;
    private Button cancelButton;
    private TimePicker timePicker;
    private Context mContext;
    private String repeat = "never";
    RadioGroup repeatRadio;
    private RadioButton radioNever;
    private RadioButton radioEveryday;
    private RadioButton radioWeekdays;
    private RadioButton radioWeekends;


    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        mContext = getContext();
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.alarm_dialogue_box, null);
        addButton = (Button) view.findViewById(R.id.buttonAdd);
        cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        repeatRadio = (RadioGroup) view.findViewById(R.id.repeat_palet);

        radioNever = (RadioButton) view.findViewById(R.id.buttonNever);
        radioEveryday = (RadioButton) view.findViewById(R.id.buttonEveryday);
        radioWeekends = (RadioButton) view.findViewById(R.id.buttonWeekdends);
        radioWeekdays = (RadioButton) view.findViewById(R.id.buttonWeekdays);

        repeatRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                if(checkedId == radioNever.getId()) radioNever.setBackgroundColor(Color.parseColor("#1ED760"));
                else radioNever.setBackgroundColor(Color.parseColor("#000000"));

                if(checkedId == radioEveryday.getId()) radioEveryday.setBackgroundColor(Color.parseColor("#1ED760"));
                else radioEveryday.setBackgroundColor(Color.parseColor("#000000"));

                if(checkedId == radioWeekdays.getId()) radioWeekdays.setBackgroundColor(Color.parseColor("#1ED760"));
                else radioWeekdays.setBackgroundColor(Color.parseColor("#000000"));

                if(checkedId == radioWeekends.getId()) radioWeekends.setBackgroundColor(Color.parseColor("#1ED760"));
                else radioWeekends.setBackgroundColor(Color.parseColor("#000000"));

            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hr;
                int min;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hr = timePicker.getHour();
                    min = timePicker.getMinute();
                }
                else {
                    hr = timePicker.getCurrentHour();
                    min = timePicker.getCurrentMinute();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hr);
                calendar.set(Calendar.MINUTE, min);
                int radioId = repeatRadio.getCheckedRadioButtonId();

                switch(radioId) {
                    case R.id.buttonNever:
                        repeat = "never";
                        break;
                    case R.id.buttonEveryday:
                        repeat = "everyday";
                        break;
                    case R.id.buttonWeekdends:
                        repeat = "weekends";
                        break;
                    case R.id.buttonWeekdays:
                        repeat = "weekdays";
                        break;
                }
                MainActivity.addAlarm(calendar, repeat);
                AlarmDialogFragment.this.getDialog().cancel();
                startMusicPlayer();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmDialogFragment.this.getDialog().cancel();
            }
        });

                // Add action buttons
                /*.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //if(hrEt.getText().length() != 0 && minEt.getText().length() != 0)
                            //MainActivity.addAlarm(hrEt.getText().toString(), minEt.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlarmDialogFragment.this.getDialog().cancel();
                    }
                });*/


        return builder.create();
    }

    private void startMusicPlayer() {
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


}
