package com.devdroid.sketchpen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ChooseColorActivity extends AppCompatActivity {

    private ColorPicker picker;
    private boolean isBackgroundColor;
    private AdView adView1, adView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_color);

        isBackgroundColor = getIntent().getBooleanExtra(Constants.KEY_BG_COLOR, false);

        MobileAds.initialize(this, getString(R.string.admob_app_id));

        //AdRequest request = Utils.newAdRequestInstance();

        int color;

        try {
            color = Utils.getIntegerPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            color = (int) Utils.getLongPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
        }


        color = (color == 0) ? Color.BLACK : color;

        picker =  findViewById(R.id.picker);

        SVBar svBar = findViewById(R.id.svbar);
        OpacityBar opacityBar = findViewById(R.id.opacitybar);
        SaturationBar saturationBar = findViewById(R.id.saturationbar);
        ValueBar valueBar = findViewById(R.id.valuebar);

        TextView textViewTitle = findViewById(R.id.textview_size_label);
        textViewTitle.setText(R.string.label_forecolor);

        View applyButton = findViewById(R.id.btn_apply);
        applyButton.setVisibility(View.VISIBLE);

        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        //To set the old selected color u can do it like this
        picker.setColor(color);
        picker.setOldCenterColor(color);
        picker.setNewCenterColor(color);

        //to turn of showing the old color
        picker.setShowOldCenterColor(true);

        picker.setColor(color);

        loadAd();
    }

    public void applyAndFinishActivity(View applyButton) {

        if (isBackgroundColor) {
            Utils.saveIntegerPreferences(this, Constants.KEY_BG_COLOR, picker.getColor());
        } else {
            Utils.saveIntegerPreferences(this, Constants.KEY_FORE_COLOR, picker.getColor());
        }
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_BG_COLOR, isBackgroundColor);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadAd() {

        adView1 = Utils.createAdView(this, Constants.AD_UNIT_ID_BANNER_COLOR_PICKER1, AdSize.MEDIUM_RECTANGLE);
        adView2 = Utils.createAdView(this, Constants.AD_UNIT_ID_BANNER_COLOR_PICKER2, AdSize.SMART_BANNER);

        LinearLayout layout = findViewById(R.id.ad_container);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = 16;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        layout.addView(adView1, params);
        layout.addView(adView2, params);

        adView1.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                repositionScrollView();
            }
        });
    }

    private void repositionScrollView() {
        if (!isFinishing()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        findViewById(R.id.scrollviewColorPicker).scrollTo(0, 0);
                    }
                }
            }, 250);
        }
    }


    public void finishActivity(View closeButton) {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        Utils.animateActivity(this, "down");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView1 != null) {
            adView1.destroy();
        }
        if (adView2 != null) {
            adView2.destroy();
        }
    }
}
