package com.sndurkin.notificationcheck;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

// This service is used solely to register the ScreenOnReceiver; it is started on application start
// and on system boot.
public class ScreenOnService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(ScreenOnReceiver.getInstance(), filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
