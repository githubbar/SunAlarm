/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunny.sunalarm;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity {

    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";

    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            } else {
                Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
                if (alarm != null && mAlarm.id == alarm.id) {
                    dismiss(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        Log.v("Sunrize Duration = " + String.valueOf(mAlarm.sunrise_duration));
        // Get the volume/camera button behavior setting
        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                        DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        updateLayout();

        // Register to get the m killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void setTitle() {
        final String titleText = mAlarm.getLabelOrDefault(this);

        TextView tv = (TextView) findViewById(R.id.alertTitle);
        tv.setText(titleText);

        setTitle(titleText);
    }

    protected int getLayoutResId() {
        return R.layout.alarm_alert_fullscreen;
    }
    
    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ViewGroup rootView = (ViewGroup)inflater.inflate(getLayoutResId(), null);
        setContentView(rootView);
        /* snooze behavior: pop a snooze confirmation view, kick alarm
           manager. */
        Button snooze = (Button) findViewById(R.id.snooze);

        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });

        /* dismiss button: close notification */
        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                    }
                });

        /* Set the title from the passed in alarm */
        setTitle();

        final int delay = mAlarm.sunrise_duration*60;
        Log.v("Sunrize Duration = " + String.valueOf(delay));
        /*If Sunrise duration is not set to 0*/
        if (delay > 0) {
            /*Enable dismiss with a single touch until the IU shows*/
            rootView.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (((ViewGroup)v).getChildAt(0).getVisibility() == View.INVISIBLE);
                                dismiss(false);
                            return true;
                        }
                    });

            /* Hide the UI until the sunrize is complete*/
            FrameLayout inner = (FrameLayout)rootView.getChildAt(0);
            inner.setVisibility(View.INVISIBLE);


            /* Show UI dialog after a delay*/
            class OneShotTask extends Thread {
                ViewGroup rootView;
                OneShotTask(ViewGroup a) { rootView = a; }
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FrameLayout inner = (FrameLayout)rootView.getChildAt(0);
                            inner.setVisibility(View.VISIBLE);
                        }
                    });
                }
            };
            Runnable task = new OneShotTask(rootView);
            ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            worker.schedule(task, delay, TimeUnit.SECONDS);

            // send MiLight UDP Socket commands
            ExecutorService service = Executors.newFixedThreadPool(4);
            service.submit(new Runnable() {
                int d = delay;
                String rgb = "20";
                String all_off = "210055";
                String all_on = "220055";
                String brighter = "230055";
                String dimmer = "240055";
                int startColor = 170;
                int COLOR_STAGES = 16;
                private void sendSymbol(String symbol){
                    try {
                        InetAddress address = InetAddress.getByName("10.0.0.6");
                        byte[] buffer = new BigInteger(symbol,16).toByteArray();
                        DatagramSocket datagramSocket = new DatagramSocket();
                        datagramSocket.send(new DatagramPacket(buffer, buffer.length, address, 8899));
                        datagramSocket.close();
                    }
                    catch (Exception ex) {
                        Log.e("Exception while communicating with a lightbulb", ex);
                    }
                }
                private void reset() {
                    for (int i=0;i<9;i++)
                        sendSymbol(dimmer);
                    sendSymbol(rgb+Integer.toHexString(startColor)+"55");
                }
                private void pause(long ms) {
                    try {
                        Thread.sleep(ms);
                    }
                    catch (InterruptedException ex) {
                        Log.e("Light bulb thread sleep interrupted", ex);
                    }
                }
                public void run() {
                    reset();
                    pause(1000);
                    sendSymbol(all_on);
                    pause(1000);
                    for (int i=1;i<=COLOR_STAGES;i++) {
                        sendSymbol(rgb + Integer.toHexString(startColor-i) + "55");
                        if (i % 4 == 0)
                            sendSymbol(brighter);
                        pause(1000*d/COLOR_STAGES);
                    }
                    //reset();
                    //sendSymbol(all_off);
                }
            });

            /*Display sunrise screen*/
            /*
            int COLOR_STAGES = 5;
            String[] color_array = getApplicationContext().getResources().getStringArray(R.array.default_color_choice_values);
            List<Animator> animators = new ArrayList<Animator>();
            for (int i=1; i < COLOR_STAGES; i++){
    //            int c1 = this.getIntent().getIntExtra("Color"+String.valueOf(i-1), 0);
    //            int c2 = this.getIntent().getIntExtra("Color" + String.valueOf(i), 0);
                int c1 = Color.parseColor(color_array[i - 1]);
                int c2 = Color.parseColor(color_array[i]);
                animators.add(ObjectAnimator.ofObject(rootView, "backgroundColor", new ArgbEvaluator(), c1, c2));
            }
            AnimatorSet a = new AnimatorSet();
            a.playSequentially(animators);
            a.setDuration((long)(1000.0*delay/COLOR_STAGES));
            a.start();
            */
        }
    }

    // Attempt to snooze this alert.
    private void snooze() {
        // Do not snooze if the snooze button is disabled.
        if (!findViewById(R.id.snooze).isEnabled()) {
            dismiss(false);
            return;
        }
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id,
                snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text,
                    Alarms.formatTime(this, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        Log.v(displayTime);

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime,
                Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
        Log.i(killed ? "Alarm killed" : "Alarm dismissed by user");
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
            stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Log.LOGV) Log.v("AlarmAlert.OnNewIntent()");

        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the alarm was deleted at some point, disable snooze.
        if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
            Button snooze = (Button) findViewById(R.id.snooze);
            snooze.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.LOGV) Log.v("AlarmAlert.onDestroy()");
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }
}
