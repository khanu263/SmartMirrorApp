package com.example.jlu.smartmirrorapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GenreActivity extends AppCompatActivity {
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
    TextView currentView, nextView, main_view, prompt_view;

    // initialize other variables
    final DataProcessing processor = new DataProcessing();
    CountDownTimer timer;
    boolean inSelectionMode = false;
    int currentRow;
    int currentColumn;

    private void clearReferences() {
        Activity currentActivity = smartMirrorApp.getCurrentActivity();
        if (this.equals(currentActivity)) {
            smartMirrorApp.setCurrentActivity(null);
            smartMirrorApp.setGenreActivity(null);
        }
    }

    public void pushHandler() {
        Intent intent = new Intent(this, RadioActivity.class);
        startActivity(intent);
    }

    public void receiveGesture(String gestureName, CountDownTimer timer) {
        Log.d("INFO", "genre received gesture");
        handleGesture(gestureName);
    }

    void handleGesture(String gestureName) {

        if (!inSelectionMode) {

            inSelectionMode = true;
            currentView = (TextView) findViewById(R.id.row_1_genre_1);
            nextView = currentView;
            smartMirrorApp.setSelectedGenre(currentView.getText().toString());
            currentRow = 1;
            currentColumn = 1;

            ((Activity) ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                    main_view.setText(getResources().getString(R.string.genre_selected, smartMirrorApp.getSelectedGenre()));
                    prompt_view.setText(R.string.genre_push);
                }
            });

        } else {

            switch (gestureName) {

                case "PUSH":
                    pushHandler();
                    break;

                case "LEFT":
                    if (currentColumn == 1) {
                        currentColumn = 3;
                        updateSelected();
                        break;
                    } else {
                        currentColumn -= 1;
                        updateSelected();
                        break;
                    }

                case "RIGHT":
                    if (currentColumn == 3) {
                        currentColumn = 1;
                        updateSelected();
                        break;
                    } else {
                        currentColumn += 1;
                        updateSelected();
                        break;
                    }

                case "UP":
                    if (currentRow == 1) {
                        currentRow = 8;
                        updateSelected();
                        break;
                    } else {
                        currentRow -= 1;
                        updateSelected();
                        break;
                    }

                case "DOWN":
                    if (currentRow == 8) {
                        currentRow = 1;
                        updateSelected();
                        break;
                    } else {
                        currentRow += 1;
                        updateSelected();
                        break;
                    }

            }

        }

    }

    void updateSelected() {

        currentView = nextView;

        String resourceID = "row_" + currentRow + "_genre_" + currentColumn;
        int resID = getResources().getIdentifier(resourceID, "id", GenreActivity.this.getPackageName());
        nextView = (TextView) findViewById(resID);
        smartMirrorApp.setSelectedGenre(nextView.getText().toString());

        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                nextView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                main_view.setText(getResources().getString(R.string.genre_selected, smartMirrorApp.getSelectedGenre()));
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartMirrorApp = (SmartMirrorApp) this.getApplicationContext();

        setContentView(R.layout.activity_genre);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        main_view = (TextView) findViewById(R.id.genre_prompt);
        prompt_view = (TextView) findViewById(R.id.genre_timer);
    }

    protected void onResume() {
        super.onResume();
        smartMirrorApp.setCurrentActivity(this);
        smartMirrorApp.setGenreActivity(this);
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
