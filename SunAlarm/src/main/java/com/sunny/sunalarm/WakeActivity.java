package com.sunny.sunalarm;

import com.sunny.sunalarm.util.SystemUiHider;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class WakeActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */

    private final int N_PHASES = 3;
    private SystemUiHider mSystemUiHider;
    private Ringtone mRingtone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_fullscreen);

        final LinearLayout view = (LinearLayout)findViewById(R.id.fullscreenImage);

        // Set up the user interaction to manually show or hide the system UI.
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                finish();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mRingtone != null)
                    mRingtone.stop();
                view.setVisibility(View.GONE);
                finish();
                return true;
            }
        });

//        ColorDrawable[] color = {new ColorDrawable(Color.BLACK), new ColorDrawable(Color.WHITE)};
//        TransitionDrawable trans = new TransitionDrawable(color);
//        trans.setCrossFadeEnabled(true);
//        controlsView.setBackgroundDrawable(trans);
//        trans.startTransition(5000);
        int delay = 0; delay = this.getIntent().getIntExtra("DELAY_SEC", delay);
        List<Animator> animators = new ArrayList<Animator>();
        for (int i=1; i < MainFragment.COLOR_STAGES; i++){
            int c1 = this.getIntent().getIntExtra("Color"+String.valueOf(i-1), 0);
            int c2 = this.getIntent().getIntExtra("Color" + String.valueOf(i), 0);
            animators.add(ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), c1, c2));
        }
        AnimatorSet a = new AnimatorSet();
        a.playSequentially(animators);
        a.setDuration((long)(1000.0*delay/N_PHASES));
        a.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if(alert == null){
                    // alert is null, using backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    if(alert == null){  // I can't see this ever being null (as always have a default notification) but just incase
                        // alert backup is null, using 2nd backup
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                }
                mRingtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
                mRingtone.play();
            }
            });
        a.start();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

}
