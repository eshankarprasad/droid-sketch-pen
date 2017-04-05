package com.devdroid.sketchpen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioGroup;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ChooseColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_color);

        int foreColor = Color.BLACK;

        try {
            foreColor = Utils.getIntegerPreferences(this, Constants.KEY_FORE_COLOR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            foreColor = (int) Utils.getLongPreferences(this, Constants.KEY_FORE_COLOR);
        }


        foreColor = foreColor == 0 ? foreColor = Color.BLACK : foreColor;

        final RadioGroup optionColor = (RadioGroup) findViewById(R.id.radiogroup_color);

        final ColorPicker picker = (ColorPicker) findViewById(R.id.picker);

        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);

        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        //To set the old selected color u can do it like this
        picker.setColor(foreColor);
        picker.setOldCenterColor(foreColor);
        picker.setNewCenterColor(foreColor);

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

    @Override
    public void finish() {
        super.finish();
        Utils.animateActivity(this, "down");
    }
}
