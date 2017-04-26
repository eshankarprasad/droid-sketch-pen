package com.devdroid.sketchpen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

public class StrokeSizeActivity extends AppCompatActivity {

    private SeekBar seekBarStrokeSize;
    private TextView textViewStrokeSize;
    private boolean eraserEnabled;
    private NativeExpressAdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroke_size);

        eraserEnabled = getIntent().getBooleanExtra(Constants.KEY_ERASER_ENABLE_DISABLE, false);

        AdRequest request = Utils.newAdRequestInstance();
        adView = (NativeExpressAdView) findViewById(R.id.adView);
        adView.loadAd(request);

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
        if (adView != null) {
            adView.destroy();
        }
    }
}
