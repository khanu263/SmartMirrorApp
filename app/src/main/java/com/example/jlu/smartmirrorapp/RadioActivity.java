package com.example.jlu.smartmirrorapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RadioActivity extends AppCompatActivity {
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

    // initialize application and context
    protected SmartMirrorApp smartMirrorApp;
    final Context ctx = this;

    // initialize TextViews
    TextView radio_date, radio_time;
    TextView info_main, info_second;

    // initialize other variables
    final DataProcessing processor = new DataProcessing();
    CountDownTimer timer;

    Map<String, String> urlMap = new HashMap<String, String>()
    {{
        put("alternative", "http://69.46.75.98:80/;");
        put("ambient", "http://37.59.28.208:8722/;");
        put("blues", "http://206.190.136.140:5022/;");
        put("classic rock", "http://us1.internet-radio.com:8105/;");
        put("classical", "http://38.100.128.106:8000/;");
        put("country", "http://198.105.216.204:8194/;");
        put("dance", "http://stream2.dancewave.online:8080/;");
        put("disco", "http://newairhost.com:8034/;");
        put("easy listening", "http://us2.internet-radio.com:8181/;");
        put("electronic", "http://198.15.94.34:8006/;");
        put("folk", "http://66.225.205.8:8000/;");
        put("grunge", "http://janus.cdnstream.com:5308/;");
        put("hip hop", "http://66.85.154.211:9224/;");
        put("indie", "http://31.14.40.21:7532/;");
        put("instrumental", "http://91.250.77.9:8060/;");
        put("jazz", "http://64.78.234.173:8240/;");
        put("metal", "http://192.99.62.212:9408/;");
        put("oldies", "http://206.217.202.1:7610/;");
        put("pop", "http://78.46.246.97:9000/;");
        put("r & b", "http://airspectrum.cdnstream1.com:8024/;");
        put("reggae", "http://64.71.79.181:9998/;");
        put("rock", "http://50.7.66.10:7030/;");
        put("soul", "http://64.95.243.43:8000/;");
        put("soundtrack", "http://149.56.23.7:20082/;");
    }};

    String selectedGenre, streamURL;
    boolean isLoading = false;

    private void clearReferences() {
        Activity currentActivity = smartMirrorApp.getCurrentActivity();
        if (this.equals(currentActivity)) {
            smartMirrorApp.setCurrentActivity(null);
            smartMirrorApp.setRadioActivity(null);
        }
    }

    public void gestureHandlerRight(CountDownTimer watchTimer) {
        watchTimer.cancel();
        Intent intent = new Intent(this, NewsActivity.class);
        startActivity(intent);
    }

    public void gestureHandlerUp(CountDownTimer watchTimer) {
        watchTimer.cancel();
        Intent intent = new Intent(this, GenreActivity.class);
        startActivity(intent);
    }

    public void gestureHandlerDown() {

        if (!smartMirrorApp.getIsPlaying() && !smartMirrorApp.getIsPaused() && !selectedGenre.equals("none")) {

            isLoading = true;

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info_main.setText(R.string.radio_loading);
                    info_second.setText(R.string.radio_wait);
                }
            });

            streamURL = urlMap.get(selectedGenre);

            String response = smartMirrorApp.playRadio(streamURL);
            if (response.equals("playing")) {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info_main.setText(R.string.radio_playing);
                        info_second.setText(selectedGenre);
                    }
                });


                smartMirrorApp.setIsPlaying(true);

            } else {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info_main.setText(R.string.radio_main_error);
                        info_second.setText(R.string.radio_second_error);
                    }
                });

            }

            isLoading = false;

        } else if (smartMirrorApp.getIsPaused() && !selectedGenre.equals("none")) {

            isLoading = true;

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info_main.setText(R.string.radio_loading);
                    info_second.setText(R.string.radio_wait);
                }
            });

            streamURL = urlMap.get(selectedGenre);

            String response = smartMirrorApp.playRadio(streamURL);
            Log.d("info", "got response");
            if (response.equals("playing")) {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info_main.setText(R.string.radio_playing);
                        info_second.setText(selectedGenre);
                    }
                });

                smartMirrorApp.setIsPlaying(true);
                smartMirrorApp.setIsPaused(false);

            } else {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info_main.setText(R.string.radio_main_error);
                        info_second.setText(R.string.radio_second_error);
                    }
                });

            }

            isLoading = false;

        } else if (smartMirrorApp.getIsPlaying() && !selectedGenre.equals("none")) {

            isLoading = true;

            String response = smartMirrorApp.pauseRadio();
            if (response.equals("paused")) {

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info_main.setText(R.string.radio_selected_genre);
                        info_second.setText(selectedGenre);
                    }
                });

                smartMirrorApp.setIsPlaying(false);
                smartMirrorApp.setIsPaused(true);
            }

            isLoading = false;

        }

    }

    public void timeoutHandler() {
        Intent intent = new Intent(this, LockscreenActivity.class);
        startActivity(intent);
    }

    public void receiveGesture(String gestureName, CountDownTimer timer) {
        timer.cancel();
        timer.start();
        if (!isLoading) {
            Log.d("INFO", "radio received gesture");
            if (gestureName.equals("RIGHT")) {
                gestureHandlerRight(timer);
            } else if (gestureName.equals("UP")) {

                if (smartMirrorApp.getIsPlaying() && !selectedGenre.equals("none")) {

                    String response = smartMirrorApp.pauseRadio();

                    if (response.equals("paused")) {

                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                info_main.setText(R.string.radio_selected_genre);
                                info_second.setText(selectedGenre);
                            }
                        });

                        smartMirrorApp.setIsPlaying(false);
                        smartMirrorApp.setIsPaused(true);
                    }

                }

                gestureHandlerUp(timer);

            } else if (gestureName.equals("DOWN")) {
                gestureHandlerDown();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartMirrorApp = (SmartMirrorApp) this.getApplicationContext();

        setContentView(R.layout.activity_radio);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        radio_date = (TextView) findViewById(R.id.radio_date);
        radio_time = (TextView) findViewById(R.id.radio_time);

        info_main = (TextView) findViewById(R.id.radio_main_info);
        info_second = (TextView) findViewById(R.id.radio_second_info);

        selectedGenre = smartMirrorApp.getSelectedGenre();

        if (!smartMirrorApp.getIsPlaying() && !smartMirrorApp.getIsPaused()) {
            info_main.setText(R.string.radio_selected_genre);
            info_second.setText(selectedGenre);
        } else if (smartMirrorApp.getIsPlaying()) {
            info_main.setText(R.string.radio_playing);
            info_second.setText(selectedGenre);
        } else if (smartMirrorApp.getIsPaused()) {
            info_main.setText(R.string.radio_selected_genre);
            info_second.setText(selectedGenre);
        }

        timer = new CountDownTimer(300000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                processor.updateMinimizedLockscreen(radio_date, radio_time, ctx);
            }

            @Override
            public void onFinish() {
                timeoutHandler();
            }
        }.start();
    }

    protected void onResume() {
        super.onResume();
        smartMirrorApp.setCurrentActivity(this);
        smartMirrorApp.setRadioActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
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
