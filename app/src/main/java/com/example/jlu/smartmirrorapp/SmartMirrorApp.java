package com.example.jlu.smartmirrorapp;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;

/**
 * Created by khanu263 on 3/23/2017.
 */
public class SmartMirrorApp extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        // initialize Particle Cloud SDK
        ParticleCloudSDK.initWithOauthCredentialsProvider(this,
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

        listenForEvents();
    }

    private Activity currentActivity = null;
    private Activity lockscreenActivity = null;
    private Activity notificationActivity = null;
    private Activity newsActivity = null;

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity receivedActivity) {
        this.currentActivity = receivedActivity;
    }

    public Activity getLockscreenActivity() {
        return lockscreenActivity;
    }

    public void setLockscreenActivity(Activity receivedActivity) {
        this.lockscreenActivity = receivedActivity;
    }

    public Activity getNotificationActivityActivity() {
        return notificationActivity;
    }

    public void setNotificationActivity(Activity receivedActivity) {
        this.notificationActivity = receivedActivity;
    }

    public Activity getNewsActivity() {
        return newsActivity;
    }

    public void setNewsActivity(Activity receivedActivity) {
        this.newsActivity = receivedActivity;
    }

    public void listenForEvents() {

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                return ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(null, new ParticleEventHandler() {

                    @Override
                    public void onEvent(String eventName, ParticleEvent particleEvent) {
                        Activity runningActivity = getCurrentActivity();

                        if (eventName.equals("GESTURE")) {

                        }
                    }

                    @Override
                    public void onEventError(Exception e) {
                        Log.e("Error", e.getMessage());
                    }

                });

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
