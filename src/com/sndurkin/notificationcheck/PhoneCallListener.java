package com.sndurkin.notificationcheck;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// This class listens for phone state changes and fires handlers for when
// a phone call is answered or missed. It's used by ScreenOnReceiver to
// make better decisions about when to vibrate when the screen is turned on.
public class PhoneCallListener extends PhoneStateListener {

    private boolean isPhoneRinging = false;
    private Observer observer;

    private static PhoneCallListener instance = new PhoneCallListener();

    public static PhoneCallListener getInstance() {
        return instance;
    }

    private PhoneCallListener() {
        super();
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch(state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isPhoneRinging = true;
                //Log.d("NotificationCheck", "CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(isPhoneRinging) {
                    if(observer != null) {
                        observer.onCallAnswered();
                    }
                }
                isPhoneRinging = false;
                //Log.d("NotificationCheck", "CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(isPhoneRinging) {
                    if(observer != null) {
                        observer.onCallMissed();
                    }
                }
                isPhoneRinging = false;
                //Log.d("NotificationCheck", "CALL_STATE_IDLE");
                break;
        }
    }

    public boolean isPhoneRinging() {
        return isPhoneRinging;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    interface Observer {
        public void onCallMissed();
        public void onCallAnswered();
    }

}