package com.sndurkin.notificationcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This receiver is used to start the service when the phone boots up.
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, ScreenOnService.class));
        }
    }
}