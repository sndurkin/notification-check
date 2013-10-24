package com.sndurkin.notificationcheck;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

// This is a notification listener service used on devices with Android 4.3+ to monitor the phone
// for notifications. When it receives a notification, it sends it to ScreenOnReceiver.
public class NotificationService extends NotificationListenerService {

    public static boolean isNotificationAccessEnabled = false;

    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = super.onBind(intent);
        isNotificationAccessEnabled = true;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        boolean retVal = super.onUnbind(intent);
        isNotificationAccessEnabled = false;
        return retVal;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        ScreenOnReceiver.getInstance().addNotificationEvent(sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        ScreenOnReceiver.getInstance().removeNotificationEvent(sbn.getPackageName());
    }

}
