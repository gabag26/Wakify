package com.sahilgaba.android.wakify;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by sahilgaba on 4/18/17.
 */

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder> {

    private Context mContext;
    private Cursor mCursor;



    public AlarmListAdapter(Context context, Cursor cursor){
        mContext = context;
        mCursor = cursor;
    }



    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int idAlarmListItem = R.layout.alarm_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachToParentImmediately = false;
        View view = inflater.inflate(idAlarmListItem, parent, attachToParentImmediately);
        AlarmViewHolder viewHolder = new AlarmViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position)) return;
        String hr = mCursor.getString(mCursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_HR));
        String min = mCursor.getString(mCursor.getColumnIndex(AlarmListContract.AlarmEntry.ALARM_TIME_MIN));
        String amPm = "AM";
        String repeat = mCursor.getString(mCursor.getColumnIndex(AlarmListContract.AlarmEntry.REPEAT));
        int hrInt = Integer.valueOf(hr);
        int minInt = Integer.valueOf(min);
        if (hrInt > 12) {
            amPm = "PM";
            hrInt -= 12;
        }
        else if (hrInt == 12) {
            amPm = "PM";
        }
        else {
            if (hrInt == 0) hrInt = 12;
        }

        if (minInt < 10) min = "0" + min;

        if (repeat.equals("never")) repeat = "Never";
        else if (repeat.equals("everyday")) repeat = "Everyday";
        else if (repeat.equals("weekends")) repeat = "Weekends";
        else repeat = "Weekdays";

        hr = Integer.toString(hrInt);
        String toggleOnOff = mCursor.getString(mCursor.getColumnIndex(AlarmListContract.AlarmEntry.TOGGLE));
        if (toggleOnOff.equals("ON")) holder.toggleSwitch.setChecked(true);
        else holder.toggleSwitch.setChecked(false);
        long id = mCursor.getLong(mCursor.getColumnIndex(AlarmListContract.AlarmEntry._ID));
        String time = hr + ":" + min;
        holder.alarmTextViewHr.setText(time);
        holder.alarmTextViewMin.setText(amPm);
        holder.repeatAlarm.setText(repeat);
        holder.itemView.setTag(id);
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void updateRecycler(Cursor newCursor) {
        if (mCursor != null) mCursor.close();

        mCursor = newCursor;

        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView alarmTextViewHr;
        TextView alarmTextViewMin;
        Switch toggleSwitch;
        TextView repeatAlarm;
        public AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTextViewHr = (TextView) itemView.findViewById(R.id.alarm_text_view_hr);
            alarmTextViewMin = (TextView) itemView.findViewById(R.id.alarm_text_view_min);
            toggleSwitch = (Switch) itemView.findViewById(R.id.switch_onoff);
            repeatAlarm = (TextView) itemView.findViewById(R.id.repeat_list_text);
            toggleSwitch.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == toggleSwitch.getId()) {
                long id = (long) this.itemView.getTag();
                MainActivity.switchToggle(id);
            }
            else {
                long id = (long) this.itemView.getTag();
                MainActivity.editListItem(id);
            }
        }
    }


}
