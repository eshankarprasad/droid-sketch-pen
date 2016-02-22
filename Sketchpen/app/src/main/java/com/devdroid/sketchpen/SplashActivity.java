package com.devdroid.sketchpen;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    startActivity(SketchPenActivity.getIntent(SplashActivity.this));
                }
            }
        }, 1500);
    }
}
