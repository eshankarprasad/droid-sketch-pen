package com.devdroid.sketchpen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewUtils;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ChooseColorActivity extends AppCompatActivity {

    private NativeExpressAdView adView;
    private ColorPicker picker;
    private boolean isBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_color);

        isBackgroundColor = getIntent().getBooleanExtra(Constants.KEY_BG_COLOR, false);

        AdRequest request = Utils.newAdRequestInstance();
        adView = (NativeExpressAdView) findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Utils.dLog("onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Utils.dLog("onAdFailedToLoad");
            }
        });
        adView.loadAd(request);

        int color = isBackgroundColor ? Color.WHITE : Color.BLACK;

        try {
            color = Utils.getIntegerPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            color = (int) Utils.getLongPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
        }


        color = color == 0 ? color = Color.BLACK : color;

        picker = (ColorPicker) findViewById(R.id.picker);

        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);
        picker.setColor(color);

        TextView textViewTitle = (TextView) findViewById(R.id.textview_size_label);
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

        /*dialogColorPicker.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                int radioButtonId = optionColor.getCheckedRadioButtonId();
                switch (radioButtonId) {
                    case R.id.radio_forecolor:
                        Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR, picker.getColor());
                        SketchPenActivity.this.mPaint.setColor(picker.getColor());
                        break;
                    case R.id.radio_backcolor:
                        Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, picker.getColor());
                        SketchPenActivity.this.drawingView.setBackgroundColor(picker.getColor());
                        break;
                }
            }
        });*/

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
        if (adView != null) {
            adView.destroy();
        }
    }
}
