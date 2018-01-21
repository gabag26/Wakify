package com.sahilgaba.android.wakify;

import android.provider.BaseColumns;

/**
 * Created by sahilgaba on 4/22/17.
 */

public class AlarmListContract {

    public static final class AlarmEntry implements BaseColumns{
        public static final String TABLE_NAME = "AlarmsList";
        public static final String ALARM_TIME_HR = "Hr";
        public static final String ALARM_TIME_MIN = "Min";
        public static final String REPEAT = "repeat";
        public static final String TOGGLE = "Toggle";
    }
}
