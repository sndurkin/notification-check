package com.sndurkin.notificationcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// This is where most of the logic for the application lives; when the SCREEN_ON event
// is fired, the phone vibrates if there are any notifications.
public class ScreenOnReceiver extends BroadcastReceiver {

    private boolean missedPhoneCall = false;
    private List<String> eventPackages = new ArrayList<String>();

    private static ScreenOnReceiver instance = new ScreenOnReceiver();

    public static ScreenOnReceiver getInstance() {
        return instance;
    }

    private ScreenOnReceiver() {
        super();
    }

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if(!missedPhoneCall) {
                // Ignore notifications that occurred before the screen shut off; we have
                // to assume the user has seen these because AFAIK there's no way to tell when
                // a notification has been dismissed by the user. We don't want to vibrate
                // for those notifications because they're no longer relevant, so we just
                // ignore all that occur before the screen shuts off.
                clearNotificationEvents();
            }
            else {
                missedPhoneCall = false;
            }
            //Log.d("NotificationCheck", "SCREEN_OFF received at " + SystemClock.uptimeMillis());
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(!preferences.getBoolean("pref_active", false)) {
                clearNotificationEvents();
                return;
            }

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // This application is only active when the ringer is set to silent or vibrate.
                clearNotificationEvents();
                return;
            }

            final PhoneCallListener phoneCallListener = PhoneCallListener.getInstance();
            if(phoneCallListener.isPhoneRinging()) {
                // If the screen is turned on because the phone is ringing,
                // the user probably isn't aware of it because the ringer
                // isn't on, so we defer vibration to the next time
                // the screen is turned on (unless the call is answered).
                phoneCallListener.addObserver(new PhoneCallListener.Observer() {
                    @Override
                    public void onCallMissed() {
                        missedPhoneCall = true;
                        phoneCallListener.removeObserver(this);
                    }

                    @Override
                    public void onCallAnswered() {
                        clearNotificationEvents();
                        phoneCallListener.removeObserver(this);
                    }
                });
                return;
            }

            //Log.d("NotificationCheck", "SCREEN_ON received at " + SystemClock.uptimeMillis() + ", checking for notifications");

            boolean atLeastOneNotification = !eventPackages.isEmpty();
            boolean vibrateForNotifications;

            int monitoringVal = Integer.parseInt(preferences.getString("pref_what", SettingsActivity.PREF_WHAT_DEFAULT));
            if(monitoringVal == SettingsActivity.WhatToCheck.ALL_NOTIFICATIONS.ordinal()) {
                //Log.d("NotificationCheck", "Will vibrate for notifications because we're monitoring all notifications");
                vibrateForNotifications = true;
            }
            else {
                vibrateForNotifications = false;
            }

            List<String> selectedPackages = NotificationListPreference.extractListFromPref(preferences.getString("pref_notifications", ""));
            for(String packageName : eventPackages) {
                if(monitoringVal != SettingsActivity.WhatToCheck.ALL_NOTIFICATIONS.ordinal()) {
                    if(selectedPackages.contains(packageName)) {
                        if(monitoringVal == SettingsActivity.WhatToCheck.ONLY_SELECTED_NOTIFICATIONS.ordinal()) {
                            //Log.d("NotificationCheck", "Will vibrate for notifications because " + packageName + " was among those selected");
                            vibrateForNotifications = true;
                        }
                    }
                    else {
                        if(monitoringVal == SettingsActivity.WhatToCheck.ALL_BUT_SELECTED_NOTIFICATIONS.ordinal()) {
                            //Log.d("NotificationCheck", "Will vibrate for notifications because " + packageName + " was NOT among those selected");
                            vibrateForNotifications = true;
                        }
                    }
                }
            }

            // Remove all notifications, so the next check will be for fresh notifications.
            clearNotificationEvents();

            if(vibrateForNotifications && atLeastOneNotification) {
                //Log.d("NotificationCheck", "Vibrating");
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
        }
    }

    public synchronized void addNotificationEvent(String packageName) {
        eventPackages.add(packageName);
    }

    public synchronized void clearNotificationEvents() {
        eventPackages.clear();
    }

}