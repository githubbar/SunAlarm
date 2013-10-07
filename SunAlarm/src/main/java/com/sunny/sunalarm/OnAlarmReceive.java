package com.sunny.sunalarm;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by oleykin on 10/3/13.
 */

public class OnAlarmReceive extends BroadcastReceiver {
    private String TAG = "OnAlarmReceived";
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock  = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE
                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyWakeLock");

        wakeLock.acquire();

        Log.d(TAG, "BroadcastReceiver, in onReceive:");
        // Start the MainActivity
        Intent newIntent = new Intent(context, WakeActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int delay=0;
        delay = intent.getIntExtra("DELAY_SEC", delay);
        newIntent.putExtra("DELAY_SEC", delay);
        for (int i=0; i < MainFragment.COLOR_STAGES; i++){
            int c = intent.getIntExtra("Color"+String.valueOf(i), 0);
            newIntent.putExtra("Color"+String.valueOf(i), c);
        }

        context.startActivity(newIntent);
        wakeLock.release();
    }
}