package com.devdroid.sketchpen;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class StrokeSizeActivity extends AppCompatActivity {

    private SeekBar seekBarStrokeSize;
    private TextView textViewStrokeSize;
    private boolean eraserEnabled;
    private AdView adView1, adView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroke_size);

        eraserEnabled = getIntent().getBooleanExtra(Constants.KEY_ERASER_ENABLE_DISABLE, false);

        TextView textViewTitle = (TextView) findViewById(R.id.textview_size_label);
        if (eraserEnabled) {
            textViewTitle.setText(getString(R.string.label_eraser_size));
        } else {
            textViewTitle.setText(getString(R.string.label_stroke_size));
        }

        textViewStrokeSize = (TextView) findViewById(R.id.textview_size);
        seekBarStrokeSize = (SeekBar) findViewById(R.id.seekbar_size);

        seekBarStrokeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                textViewStrokeSize.setText((progress == 0 ? 1 : progress) + "");
                if (progress == 0) {
                    seekBar.setProgress(1);
                }
            }
        });

        int savedStrokeSize = Utils.getIntegerPreferences(this, Constants.KEY_STROKE_SIZE);
        int savedEraserSize = Utils.getIntegerPreferences(this, Constants.KEY_ERASER_SIZE);

        if (eraserEnabled) {
            seekBarStrokeSize.setProgress(savedEraserSize == 0 ? Constants.DEFAULT_ERASER_SIZE : savedEraserSize);
        } else {
            seekBarStrokeSize.setProgress(savedStrokeSize == 0 ? Constants.DEFAULT_STROKE_SIZE : savedStrokeSize);
        }

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStrokeSize();
                setResult(RESULT_OK);
                finish();
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStrokeSize();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        loadAd();
    }

    private void loadAd() {

        adView1 = Utils.createAdView(this, Constants.AD_UNIT_ID_BANNER_STROKE_SIZE1, AdSize.MEDIUM_RECTANGLE);
        adView2 = Utils.createAdView(this, Constants.AD_UNIT_ID_BANNER_STROKE_SIZE2, AdSize.SMART_BANNER);

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
                        findViewById(R.id.scrollviewStrokeSize).scrollTo(0, 0);
                    }
                }
            }, 250);
        }
    }

    private void saveStrokeSize() {
        if (eraserEnabled) {
            Utils.saveIntegerPreferences(this, Constants.KEY_ERASER_SIZE, seekBarStrokeSize.getProgress());
        } else {
            Utils.saveIntegerPreferences(this, Constants.KEY_STROKE_SIZE, seekBarStrokeSize.getProgress());
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
