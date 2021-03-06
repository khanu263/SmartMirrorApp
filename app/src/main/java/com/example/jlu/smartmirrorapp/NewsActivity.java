package com.example.jlu.smartmirrorapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class NewsActivity extends AppCompatActivity {
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

    // initialize TextView
    TextView news_date;
    TextView news_time;
    TextView news_attribution;
    TextView news_default;
    TextView headline_1;
    TextView date_1;
    TextView headline_2;
    TextView date_2;
    TextView headline_3;
    TextView date_3;
    TextView headline_4;
    TextView date_4;
    TextView headline_5;
    TextView date_5;

    // initialize LinearLayout
    LinearLayout headline_layout;
    LinearLayout fullscreen_content;

    // initialize other variables
    final DataProcessing processor = new DataProcessing();
    CountDownTimer timer;
    String headlineString;
    String[][] headlineArray;

    private void clearReferences() {
        Activity currentActivity = smartMirrorApp.getCurrentActivity();
        if (this.equals(currentActivity)) {
            smartMirrorApp.setCurrentActivity(null);
            smartMirrorApp.setNewsActivity(null);
        }
    }

    class RetrieveNewsHeadlines extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {}

        protected String doInBackground(Void... urls) {

            try {

                URL url = new URL("https://newsapi.org/v1/articles?source=reuters&apiKey=75f3a5ec633a443abd83eb7df6670618");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    bufferedReader.close();
                    return stringBuilder.toString();

                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {

            if (response == null || response.substring(11, 16).equals("error")) {
                news_default.setText(ctx.getResources().getString(R.string.news_error));
            } else {
                Log.d("Info", "received headlines");
                headlineString = response;
                headlineArray = processor.parseHeadlineJSON(headlineString, ctx);
                processor.displayHeadlines(headlineArray, fullscreen_content, news_attribution, news_default, headline_1, date_1, headline_2, date_2, headline_3, date_3, headline_4, date_4, headline_5, date_5, ctx);
            }

        }

    }

    public void gestureHandlerRight(CountDownTimer watchTimer) {
        watchTimer.cancel();
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void gestureHandlerLeft(CountDownTimer watchTimer) {
        watchTimer.cancel();
        Intent intent = new Intent(this, RadioActivity.class);
        startActivity(intent);
    }

    public void timeoutHandler() {
        Intent intent = new Intent(this, LockscreenActivity.class);
        startActivity(intent);
    }

    public void receiveGesture(String gestureName, CountDownTimer timer) {
        timer.cancel();
        timer.start();
        Log.d("INFO", "news received gesture");
        if (gestureName.equals("RIGHT")) {
            gestureHandlerRight(timer);
        } else if (gestureName.equals("LEFT")) {
            gestureHandlerLeft(timer);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartMirrorApp = (SmartMirrorApp) this.getApplicationContext();

        setContentView(R.layout.activity_news);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // set text views for the activity
        news_date = (TextView) findViewById(R.id.news_date);
        news_time = (TextView) findViewById(R.id.news_time);
        news_attribution = (TextView) findViewById(R.id.news_attribution);
        news_default = (TextView) findViewById(R.id.news_default);
        headline_1 = (TextView) findViewById(R.id.headline_1);
        date_1 = (TextView) findViewById(R.id.date_1);
        headline_2 = (TextView) findViewById(R.id.headline_2);
        date_2 = (TextView) findViewById(R.id.date_2);
        headline_3 = (TextView) findViewById(R.id.headline_3);
        date_3 = (TextView) findViewById(R.id.date_3);
        headline_4 = (TextView) findViewById(R.id.headline_4);
        date_4 = (TextView) findViewById(R.id.date_4);
        headline_5 = (TextView) findViewById(R.id.headline_5);
        date_5 = (TextView) findViewById(R.id.date_5);

        // set linear layout
        headline_layout = (LinearLayout) findViewById(R.id.headline_layout);
        fullscreen_content = (LinearLayout) findViewById(R.id.fullscreen_content);

        timer = new CountDownTimer(300000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                processor.updateMinimizedLockscreen(news_date, news_time, ctx);
            }

            @Override
            public void onFinish() {
                timeoutHandler();
            }
        }.start();

        new RetrieveNewsHeadlines().execute();
    }

    protected void onResume() {
        super.onResume();
        smartMirrorApp.setCurrentActivity(this);
        smartMirrorApp.setNewsActivity(this);
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
