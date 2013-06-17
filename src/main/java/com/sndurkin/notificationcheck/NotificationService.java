package com.sndurkin.notificationcheck;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Sean on 6/11/13.
 */
public class NotificationService extends AccessibilityService {

    public static final String SERVICE_NAME = "com.sndurkin.notificationcheck/com.sndurkin.notificationcheck.NotificationService";

    private boolean isInit = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("NotificationCheck", "Notification received from " + event.getPackageName() + " at " + event.getEventTime() + ": " + event.getText());
        if(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            ScreenOnReceiver.getInstance().addNotificationEvent(event.getPackageName().toString(), event.getEventTime());
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
