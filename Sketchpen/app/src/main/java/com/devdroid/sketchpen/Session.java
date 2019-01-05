package com.devdroid.sketchpen;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class Session extends Application {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        //FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().subscribeToTopic("all_user_sketchpen_debug");
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("all_user_sketchpen_release");
        }
    }

    public FirebaseAnalytics getGAInstance() {

        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return mFirebaseAnalytics;
    }
}
