package com.sndurkin.notificationcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

// This is used solely to start the PhoneCallListener.
public class PhoneCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(PhoneCallListener.getInstance(), PhoneStateListener.LISTEN_CALL_STATE);
    }

}
