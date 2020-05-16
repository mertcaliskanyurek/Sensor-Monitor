package com.mertcaliskanyurek.sensormonitor.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mertcaliskanyurek.sensormonitor.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public final class AboveApi26Strategy implements NotificationStrategy {

    public static final String CHANNEL_NAME = "NotificationChannel";

    private Context mContext;
    private NotificationManager mNotiManager;

    public AboveApi26Strategy(Context context){
        this.mContext = context;
        mNotiManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            initChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initChannel()
    {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_NAME,
                "Sensor Monitor Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        mNotiManager = mContext.getSystemService(NotificationManager.class);
        mNotiManager.createNotificationChannel(serviceChannel);
    }

    @Override
    public Notification buildNotification(String title, String text, int smallIcon) {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);

        return new NotificationCompat.Builder(mContext, CHANNEL_NAME)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(smallIcon)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setOnlyAlertOnce(true)
                .build();
    }

    @Override
    public void notify(int id,Notification notification) {
        mNotiManager.notify(id,notification);
    }

}
