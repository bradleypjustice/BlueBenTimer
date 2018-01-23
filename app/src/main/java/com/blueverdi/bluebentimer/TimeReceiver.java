package com.blueverdi.bluebentimer;

/**
 * Created by brad on 1/16/18.
 * based on http://www.devexchanges.info/2016/08/the-principle-of-using-countdowntimer.html
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class TimeReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.d(TAG, "received timer");
        Intent i = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);

        Notification.Builder b = new Notification.Builder(context);
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/hourlychimebeg");
        b.setSound(alarmSound);
        b.setSound(alarmSound)
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.timer_complete))
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)
                .setContentIntent(pIntent);

        Notification n = b.build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, n);
        PrefUtils prefUtils = new PrefUtils(context);
        prefUtils.setTimerData(0,prefUtils.getTimerInSecs());
    }
}
