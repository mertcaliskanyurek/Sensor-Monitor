package com.mertcaliskanyurek.sensormonitor.Data;

import android.provider.BaseColumns;

public final class DatabaseContract {

    public static final String ACTIVITY_TABLE_NAME = "SensorActivity";

    public static final class ACTIVITY_ENTRY implements BaseColumns {

        public static final String COLMN_ID = BaseColumns._ID;
        public static final String COLMN_SENSOR_TYPE = "SensorType";
        public static final String COLMN_VALUE = "Value";
        public static final String COLMN_TIME= "Time";
    }

}
