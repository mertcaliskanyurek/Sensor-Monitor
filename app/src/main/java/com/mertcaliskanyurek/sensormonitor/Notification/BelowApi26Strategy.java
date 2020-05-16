package com.mertcaliskanyurek.sensormonitor.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public final class BelowApi26Strategy implements NotificationStrategy {
    private Context mContext;

    public BelowApi26Strategy(Context context){
        this.mContext = context;
    }

    @Override
    public Notification buildNotification(String stitle, String text, int smallIcon) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(smallIcon)
                .setContentTitle(stitle)
                .setContentText(text)
                .build();
    }

    @Override
    public void notify(int id,Notification notification) {
        ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);
    }
}
