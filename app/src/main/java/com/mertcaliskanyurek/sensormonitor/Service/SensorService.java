package com.mertcaliskanyurek.sensormonitor.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mertcaliskanyurek.sensormonitor.Data.DatabaseHelper;
import com.mertcaliskanyurek.sensormonitor.Notification.AboveApi26Strategy;
import com.mertcaliskanyurek.sensormonitor.Notification.BelowApi26Strategy;
import com.mertcaliskanyurek.sensormonitor.Notification.NotificationStrategy;
import com.mertcaliskanyurek.sensormonitor.R;
import com.mertcaliskanyurek.sensormonitor.Sensor.SensorActivityListener;
import com.mertcaliskanyurek.sensormonitor.Sensor.SystemSensorManager;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public final class SensorService extends Service implements SensorActivityListener {

    private static final String TAG = SensorService.class.getSimpleName();

    private static final int PERMANENT_NOTI_ID = 1;

    private final ServiceBinder mServiceBinder = new ServiceBinder();

    private SystemSensorManager mSensorManager;

    private NotificationStrategy mNotificationStrategy;
    private DatabaseHelper mDbHelper;
    private ServiceListener mListener;

    private Map<String,Float> mLastValues;

    private boolean mServiceStarted =false;
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mNotificationStrategy = new AboveApi26Strategy(this);
        else
            mNotificationStrategy = new BelowApi26Strategy(this);
        mDbHelper = new DatabaseHelper(this);
        mSensorManager = new SystemSensorManager(this);

        mLastValues = new Hashtable<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.setListener(this);
        mSensorManager.registerSensors();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String text = getNotiText();
            Notification notification = mNotificationStrategy.
                    buildNotification(getString(R.string.noti_title),text,R.drawable.ic_launcher_foreground);

            startForeground(PERMANENT_NOTI_ID,notification);
            mServiceStarted = true;
            Log.i(TAG,"Service Started with START_NOT_STICKY! Reciever registered");
            return START_NOT_STICKY;
        }

        mServiceStarted = true;
        Log.i(TAG,"Service Started with START_STICKY! Reciever registered");
        return START_STICKY;
    }

    private String getNotiText()
    {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String,Float> entry: mLastValues.entrySet())
            builder.append(entry.getKey() + " " +entry.getValue() + "\n");
        return builder.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
        Log.i(TAG,"Service Destroyed! Reciever unregistered");
    }

    @Override
    public void onActivity(Sensor sensor, float value) {
        Log.i(TAG,"On Activity: "+value);

        Float lastVal = mLastValues.get(sensor.getName());
        if(lastVal == null)
            handleValueChange(sensor,value);
        else if(lastVal != value)
            handleValueChange(sensor,value);
    }

    private void handleValueChange(Sensor sensor, float value)
    {
        mDbHelper.insertActivity(sensor.getType(), value);
        mLastValues.put(sensor.getName(),value);

        String text = getNotiText();
        Notification notification = mNotificationStrategy.
                buildNotification(getString(R.string.noti_title),text,R.drawable.ic_launcher_foreground);
        mNotificationStrategy.notify(PERMANENT_NOTI_ID,notification);

        if(mListener != null)
            mListener.onChanged();
    }

    public List<Sensor> getSensors()
    {
        return mSensorManager.getSensors();
    }

    public float getSensorValue(String sensorName)
    {
        Float value = mLastValues.get(sensorName);
        if(value != null)
            return value;
        return -1;
    }

    public boolean isServiceStarted() {
        return mServiceStarted;
    }

    public void stopService()
    {
        mServiceStarted = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true);
        stopSelf();
        mSensorManager.unregisterSensors();
    }

    public void setListener(ServiceListener listener) {
        this.mListener = listener;
    }

    public class ServiceBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }



}
