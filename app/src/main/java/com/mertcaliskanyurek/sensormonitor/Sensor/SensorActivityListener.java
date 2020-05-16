package com.mertcaliskanyurek.sensormonitor.Sensor;

import android.hardware.Sensor;

public interface SensorActivityListener {
    void onActivity(Sensor sensor, float value);
}
