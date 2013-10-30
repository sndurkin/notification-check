package com.sndurkin.notificationcheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
        if(!sbn.isClearable()) {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final int prefNotificationType = Integer.parseInt(preferences.getString("pref_notification_type", "0"));
            if(prefNotificationType == SettingsActivity.NotificationType.NEW_NON_PERSISTED_NOTIFICATIONS.ordinal()
                    || prefNotificationType == SettingsActivity.NotificationType.NON_PERSISTED_NOTIFICATIONS_UNTIL_DISMISSED.ordinal()) {
                // User wants to ignore persisted notifications.
                return;
            }
        }

        Intent intent = new Intent(ScreenOnReceiver.NOTIFICATION_POSTED_INTENT);
        intent.putExtra("packageName", sbn.getPackageName());
        intent.putExtra("id", sbn.getId());
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Intent intent = new Intent(ScreenOnReceiver.NOTIFICATION_REMOVED_INTENT);
        intent.putExtra("packageName", sbn.getPackageName());
        intent.putExtra("id", sbn.getId());
        sendBroadcast(intent);
    }

}
