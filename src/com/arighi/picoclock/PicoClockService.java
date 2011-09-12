package com.arighi.picoclock.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.IBinder;
import android.util.Log;

public class PicoClockService extends Service {

    private static final String LOG_TAG = "PicoClock";

    private ScreenReceiver sr = null;

    private class ScreenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                PicoClockProvider.timerOff(context);
                Log.d(LOG_TAG, "screen off");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                PicoClockProvider.timerOn(context);
                Log.d(LOG_TAG, "screen on");
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onCreate() {
        Log.d(LOG_TAG, "start screen service");
        sr = new ScreenReceiver();
        registerReceiver(sr, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(sr, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    public void onDestroy() {
        Log.d(LOG_TAG, "stop screen service");
        unregisterReceiver(sr);
        sr = null;
    }
}
