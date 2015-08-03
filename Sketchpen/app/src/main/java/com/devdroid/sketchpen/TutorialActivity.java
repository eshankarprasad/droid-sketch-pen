package com.devdroid.sketchpen;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;


public class TutorialActivity extends ActionBarActivity {

    public static Intent getIntent(Activity activity) {
        Intent intent = new Intent(activity, TutorialActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tutorial);
    }

    public void hideTutorial(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
