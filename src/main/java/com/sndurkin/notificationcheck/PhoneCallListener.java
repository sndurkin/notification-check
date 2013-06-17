package com.sndurkin.notificationcheck;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 6/15/13.
 */
public class PhoneCallListener extends PhoneStateListener {

    private boolean isPhoneRinging = false;
    private List<Observer> observers = new ArrayList<Observer>();

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
                Log.d("NotificationCheck", "CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(isPhoneRinging) {
                    for(Observer observer : observers) {
                        observer.onCallAnswered();
                    }
                }
                isPhoneRinging = false;
                Log.d("NotificationCheck", "CALL_STATE_RINGING");
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(isPhoneRinging) {
                    for(Observer observer : observers) {
                        observer.onCallMissed();
                    }
                }
                isPhoneRinging = false;
                Log.d("NotificationCheck", "CALL_STATE_IDLE");
                break;
        }
    }

    public boolean isPhoneRinging() {
        return isPhoneRinging;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    interface Observer {
        public void onCallMissed();
        public void onCallAnswered();
    }

}
