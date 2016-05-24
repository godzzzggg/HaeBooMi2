package com.hbm.haeboomi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BroadcastD extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;

    public void onReceive(Context context,Intent intent){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context,0,new Intent(context,StudentMainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.logo).setTicker("HeTT").setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle("알람").setContentText("PUSH!!")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true).setOngoing(false);

        notificationManager.notify(1,builder.build());


    }
}
