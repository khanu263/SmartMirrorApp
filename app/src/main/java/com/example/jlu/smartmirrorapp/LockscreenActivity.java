package com.example.jlu.smartmirrorapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.MessagePattern;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LockscreenActivity extends AppCompatActivity {
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
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    public void gestureHandler(CountDownTimer watchTimer, long subscriptionID) {
        watchTimer.cancel();

        try {
            ParticleCloudSDK.getCloud().unsubscribeFromEventWithID(subscriptionID);
        } catch (ParticleCloudException e) {
            Log.e("ERROR", e.getBestMessage());
        }


        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void subscribeToEvents(final CountDownTimer watchTimer) {
        // subscribes to events and fires the correct handlers for each gesture

        final long subscriptionID = 0;

        if (subscriptionID == 0) {

            Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

                @Override
                public Object callApi(final ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                    particleCloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {

                        @Override
                        public void onEvent(String eventName, ParticleEvent particleEvent) {
                            if (eventName.equals("GESTURE")) {
                                gestureHandler(watchTimer, subscriptionID);
                            }
                        }

                        @Override
                        public void onEventError(Exception e) {
                            Log.e("Error", e.getMessage());
                        }

                    });

                    return "done";

                }

                public void onSuccess(Object o) {

                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    String errorMessage = e.getBestMessage();
                    Log.e("Error", errorMessage, e);
                }

            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lockscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // initialize context and class
        final Context ctx = this;
        final DataProcessing processor = new DataProcessing();

        // initialize Particle Cloud SDK
        ParticleCloudSDK.initWithOauthCredentialsProvider(ctx,
                new ApiFactory.OauthBasicAuthCredentialsProvider() {

                    public String getClientId() {
                        return "smartmirrorapp-6563";
                    }

                    public String getClientSecret() {
                        return "8374ad857562ff5010736b9578c874710aedba9f";
                    }
                });

        // Login to Particle Cloud
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Integer>() {

            @Override
            public Integer callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn("hiflyer380@hotmail.com", "Wwdcado_786");
                return 1;
            }

            public void onSuccess(Integer i) {
                if (i == 1) {
                    Log.d("Particle", "Login successful.");
                } else {
                    Log.e("Particle", "An unknown error occurred.");
                }
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                String errorMessage = e.getBestMessage();
                Log.e("Error", errorMessage, e);
            }

        });

        // initialize text views for the activity
        final TextView greeting_text = (TextView) findViewById(R.id.greeting_text);
        final TextView period_text = (TextView) findViewById(R.id.period_text);
        final TextView date_text = (TextView) findViewById(R.id.date_text);
        final TextView time_text = (TextView) findViewById(R.id.time_text);

        CountDownTimer timer = new CountDownTimer(1000000000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                processor.updateLockscreen(greeting_text, period_text, date_text, time_text, ctx);
            }

            @Override
            public void onFinish() {
                Log.d("noot", "noot");
            }
        }.start();

        // start event listening
        subscribeToEvents(timer);

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

}
