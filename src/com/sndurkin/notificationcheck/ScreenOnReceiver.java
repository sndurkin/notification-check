package com.sndurkin.notificationcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// This is where most of the logic for the application lives; when the SCREEN_ON event
// is fired, the phone vibrates if there are any notifications.
public class ScreenOnReceiver extends BroadcastReceiver {

    private boolean missedPhoneCall = false;
    private Set<String> eventPackages = new HashSet<String>();

    // Singleton pattern
    private static ScreenOnReceiver instance = new ScreenOnReceiver();
    public static ScreenOnReceiver getInstance() {
        return instance;
    }
    private ScreenOnReceiver() {
        super();
    }

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if(!missedPhoneCall) {
                requestToClearNotificationEvents(preferences);
            }
            else {
                missedPhoneCall = false;
            }
            //Log.d("NotificationCheck", "SCREEN_OFF received at " + SystemClock.uptimeMillis());
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if(!preferences.getBoolean("pref_active", false)) {
                requestToClearNotificationEvents(preferences);
                return;
            }

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // This application is only active when the ringer is set to silent or vibrate.
                requestToClearNotificationEvents(preferences);
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
                        requestToClearNotificationEvents(preferences);
                        phoneCallListener.removeObserver(this);
                    }
                });
                return;
            }

            boolean atLeastOneNotification = !eventPackages.isEmpty();
            boolean vibrateForNotifications;

            int prefWhatToCheck = Integer.parseInt(preferences.getString("pref_what", "0"));
            if(prefWhatToCheck == SettingsActivity.WhatToCheck.ALL_NOTIFICATIONS.ordinal()) {
                Log.d("NotificationCheck", "Will vibrate for notifications because we're monitoring all notifications");
                vibrateForNotifications = true;
            }
            else {
                vibrateForNotifications = false;

                List<String> selectedPackages = NotificationListPreference.extractListFromPref(preferences.getString("pref_notifications", ""));
                for(String packageName : eventPackages) {
                    if(selectedPackages.contains(packageName)) {
                        if(prefWhatToCheck == SettingsActivity.WhatToCheck.ONLY_SELECTED_NOTIFICATIONS.ordinal()) {
                            Log.d("NotificationCheck", "Will vibrate for notifications because " + packageName + " was among those selected");
                            vibrateForNotifications = true;
                        }
                    }
                    else {
                        if(prefWhatToCheck == SettingsActivity.WhatToCheck.ALL_BUT_SELECTED_NOTIFICATIONS.ordinal()) {
                            Log.d("NotificationCheck", "Will vibrate for notifications because " + packageName + " was NOT among those selected");
                            vibrateForNotifications = true;
                        }
                    }
                }
            }

            requestToClearNotificationEvents(preferences);

            if(vibrateForNotifications && atLeastOneNotification) {
                Log.d("NotificationCheck", "Vibrating");
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
        }
    }

    public synchronized void addNotificationEvent(String packageName) {
        eventPackages.add(packageName);
    }

    public synchronized void removeNotificationEvent(String packageName) {
        eventPackages.remove(packageName);
    }

    // This method is used to ignore notifications that occurred before the screen shut off. On Android < 4.3,
    // we have to assume the user has seen these because there's no way to tell when a notification has been
    // dismissed by the user.
    //
    // If we're working with Android 4.3+, we can tell when notifications are dismissed, so the user dictates
    // whether notifications are ignored by Notification Check via the When To Vibrate preference.
    public synchronized void requestToClearNotificationEvents(SharedPreferences preferences) {
        int prefWhenToVibrate = Integer.parseInt(preferences.getString("pref_when", "0"));
        if(prefWhenToVibrate == SettingsActivity.WhenToVibrate.ONLY_NEW_NOTIFICATIONS.ordinal()) {
            eventPackages.clear();
        }
    }

}