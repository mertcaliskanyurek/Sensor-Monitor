package com.mertcaliskanyurek.sensormonitor.Sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class SystemSensorManager implements SensorEventListener {

    private static final String TAG = SystemSensorManager.class.getSimpleName();

    public static final int[] SENSOR_TYPES = {
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_RELATIVE_HUMIDITY
    };

    private SensorManager mSensorManager;
    private List<Sensor> mSensors;
    private SensorActivityListener mListener;

    public SystemSensorManager(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensors = new ArrayList<>();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(mListener != null)
            mListener.onActivity(event.sensor, event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerSensors()
    {
        if(mListener == null)
            Log.w(TAG,"You have not set the SensorActivityListener. You may not get sensor values.");

        Sensor sensor;
        for(int type: SENSOR_TYPES)
        {
            sensor = mSensorManager.getDefaultSensor(type);
            mSensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
            mSensors.add(sensor);
        }
    }

    public void unregisterSensors()
    {
        mSensorManager.unregisterListener(this);
    }

    public void setListener(SensorActivityListener listener) {
        this.mListener = listener;
    }

    public List<Sensor> getSensors() {
        return mSensors;
    }
}
