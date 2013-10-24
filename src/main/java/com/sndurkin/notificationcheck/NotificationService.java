package com.sndurkin.notificationcheck;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

// This is an accessibility service used to monitor the phone for notifications. When it
// receives one, it sends it to ScreenOnReceiver.
public class NotificationService extends AccessibilityService {

    public static final String SERVICE_NAME = "com.sndurkin.notificationcheck/com.sndurkin.notificationcheck.NotificationService";

    private boolean isInit = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.d("NotificationCheck", "Notification received from " + event.getPackageName() + " at " + event.getEventTime() + ": " + event.getText());
        if(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            ScreenOnReceiver.getInstance().addNotificationEvent(event.getPackageName().toString());
        }
    }

    @Override
    protected void onServiceConnected() {
        if (isInit) {
            return;
        }
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        isInit = true;
    }

    @Override
    public void onInterrupt() {
        isInit = false;
    }

}
