package com.devdroid.sketchpen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.DrawingView;
import com.devdroid.sketchpen.utility.Utils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.File;
import java.io.FileOutputStream;

import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;

@SuppressLint("NewApi")
public class SketchPenActivity extends ActionBarActivity  implements MediaScannerConnection.MediaScannerConnectionClient {

    private static final String TAG = SketchPenActivity.class.getSimpleName();
    // Test ITEM_SKU used for InAppPurchase the app
    //private static final String ITEM_SKU = "android.test.purchased";
    //private static final String ITEM_SKU = "android.test.cancelled";
    //private static final String ITEM_SKU = "android.test.refunded";
    //private static final String ITEM_SKU = "android.test.item_unavailable";

    // Production
    private static final String ITEM_SKU = "com.devdroid.sketchpen.adfree";
    private static final int SELECT_PICTURE = 1;

    private DrawingView drawingView;
    private Paint mPaint;
    private String mediaScanFilePath;
    private ProgressDialog progress;
    private MediaScannerConnection conn;
    private int sdk;
    private boolean eraserEnabled;
    private AdView adView;
    private InterstitialAd interstitial;
    private boolean flagAdFree;
    private IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sdk = android.os.Build.VERSION.SDK_INT;

        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {

            // Enabling full screen mode
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_sketch_pen);
        loadDrawingView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sketch_pen, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (eraserEnabled) {

            menu.getItem(0).setVisible(false); // "Color" will be hiden
            menu.getItem(2).setVisible(false); // "Enable eraser" will be hiden
            menu.getItem(3).setVisible(true);  // "Disable eraser" will be shown
        } else {
            menu.getItem(0).setVisible(true); // "Color" will be shown
            menu.getItem(2).setVisible(true);  // "Enable eraser" will be shown
            menu.getItem(3).setVisible(false); // "Disable eraser" will be hidden
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_stroke_color) {
            //Toast.makeText(SketchPenActivity.this, "Stroke color.", Toast.LENGTH_SHORT).show();
            showStrokeColorDialog(SketchPenActivity.this);
            return true;
        } else if (item.getItemId() == R.id.action_stroke_size) {
            showStrokeSizeDialog(SketchPenActivity.this);
            return true;
        } else if(item.getItemId() == R.id.action_view_images) {
            viewImages();
            return true;
        } else if(item.getItemId() == R.id.action_save_image) {
            if(!"".equals(saveImage())) {
                Utils.showToast(SketchPenActivity.this, getString(R.string.toast_save_image_success), Toast.LENGTH_SHORT);
            }
        } else if (item.getItemId() == R.id.action_save_share) {
            saveImageAndShareWithFriend();
        } else if (item.getItemId() == R.id.action_insert_image) {
            insertImage();
        } else if (item.getItemId() == R.id.action_reset) {
            resetImage();
        } else if(item.getItemId() == R.id.action_rate) {
            rate();
        } else if (item.getItemId() == R.id.action_like) {
            like();
        } else if (item.getItemId() == R.id.action_enable_eraser) {
            enableEraser();
        } else if (item.getItemId() == R.id.action_disable_eraser) {
            disableEraser();
        } else if (item.getItemId() == R.id.action_buy) {
            if(Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {
                buyAdFree();
            } else {
                Utils.showToast(SketchPenActivity.this, "Already bought", Toast.LENGTH_LONG);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showStrokeColorDialog(SketchPenActivity activity) {

        int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        foreColor = foreColor == 0 ? foreColor = Color.BLACK : foreColor;

        final Dialog dialogColorPicker = new Dialog(SketchPenActivity.this);
        //dialogColorPicker.setTitle(getString(R.string.label_forecolor));
        dialogColorPicker.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialogColorPicker.setContentView(R.layout.dialog_color_picker);

        final RadioGroup optionColor = (RadioGroup) dialogColorPicker.findViewById(R.id.radiogroup_color);

        final ColorPicker picker = (ColorPicker) dialogColorPicker.findViewById(R.id.picker);

        SVBar svBar = (SVBar) dialogColorPicker.findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) dialogColorPicker.findViewById(R.id.opacitybar);
        SaturationBar saturationBar = (SaturationBar) dialogColorPicker.findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) dialogColorPicker.findViewById(R.id.valuebar);

        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        //To set the old selected color u can do it like this
        picker.setColor(foreColor);
        picker.setOldCenterColor(foreColor);
        picker.setNewCenterColor(foreColor);

        //Log.d(TAG, buttonForeColor.getTag() + " buttonForeColor.getTag()");

        //to turn of showing the old color
        picker.setShowOldCenterColor(true);

        dialogColorPicker.setOnDismissListener(new DialogInterface.OnDismissListener() {

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
                //Log.d(TAG, "picker.getColor():" + picker.getColor());
            }
        });
        dialogColorPicker.show();
    }

    private void showStrokeSizeDialog(FragmentActivity activity) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setTitle(getString(R.string.label_size));
        dialog.setContentView(R.layout.dialog_stroke_size);
        dialog.show();

        TextView textViewTitle = (TextView) dialog.findViewById(R.id.textview_size_label);
        if (eraserEnabled) {
            textViewTitle.setText(getString(R.string.label_eraser));
        } else {
            textViewTitle.setText(getString(R.string.label_size));
        }

        final TextView textViewStrokeSize = (TextView) dialog.findViewById(R.id.textview_size);
        final SeekBar seekBarStrokeSize = (SeekBar) dialog.findViewById(R.id.seekbar_size);

        //seekBarStrokeSize.setProgress(size == 0 ? 12 : size);
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
                if(progress == 0) {
                    seekBar.setProgress(1);
                }
                SketchPenActivity.this.mPaint.setStrokeWidth(progress);
            }
        });

        int savedStrokeSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int savedEraserSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);

        if(eraserEnabled) {
            seekBarStrokeSize.setProgress(savedEraserSize == 0 ? Constants.DEFAULT_ERASER_SIZE : savedEraserSize);
        } else {
            seekBarStrokeSize.setProgress(savedStrokeSize == 0 ? Constants.DEFAULT_STROKE_SIZE : savedStrokeSize);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (eraserEnabled) {
                    Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE, seekBarStrokeSize.getProgress());
                } else {
                    Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE, seekBarStrokeSize.getProgress());
                }
            }
        });
    }

    private void viewImages() {

        String folderPath = Environment.getExternalStorageDirectory().getPath() +"/sketchpen/";

        File folder = new File(folderPath);

        if(!folder.exists()) {
            Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found), Toast.LENGTH_LONG);
            return;
        }

        String[] allFiles = folder.list();

        if(allFiles.length > 0) {
            mediaScanFilePath = Environment.getExternalStorageDirectory().toString()+"/sketchpen/"+allFiles[allFiles.length-1];
        } else {
            Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found), Toast.LENGTH_LONG);
            return;
        }
        if(allFiles.length > 0) {
            progress = ProgressDialog.show(this,
                    getString(R.string.dialogue_scanning_media_title),
                    getString(R.string.dialogue_scanning_media_body));
            startScan();
        } else {
            Toast.makeText(this, getString(R.string.toast_scanning_media_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImage() {

        String imagePath = "";

        drawingView.setDrawingCacheEnabled(true);
        drawingView.buildDrawingCache(true);
        Bitmap screenshot = Bitmap.createBitmap(drawingView.getDrawingCache());
        drawingView.setDrawingCacheEnabled(false);

        File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/sketchpen");
        if(!myFile.exists()) {
            myFile.mkdir();
        }

        int newImageCount = Utils.getIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER) + 1;
        imagePath = myFile.getAbsolutePath() + "/sketch-pen00" + newImageCount + ".png";
        Utils.saveIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER, newImageCount);

        try {
            // Start refreshing galary
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imagePath);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            // End refreshing galary
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            if ( fos != null ) {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
        } catch( Exception e ) {
            imagePath = "";
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.toast_save_image_error), Toast.LENGTH_SHORT).show();
            Utils.saveIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER, newImageCount-1);
        }
        return imagePath;
    }

    private void saveImageAndShareWithFriend() {

        String imagePath = saveImage();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imagePath));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_image_text));
        startActivityForResult(Intent.createChooser(intent , getString(R.string.chooser_share_title)),0);
    }

    private void like() {

        String facebookUrl = "https://www.facebook.com/sketchpen000";
        try {
            int versionCode = getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } else {
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb:/profile/334200140064180")));
            }

        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(facebookUrl)));
        }
    }

    private void rateYourAppAutomatically() {

        Long rateYourApp = Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);

        if(!Utils.hasConnection(SketchPenActivity.this)) {
            // No internet connectivity
            return;
        }

        if(rateYourApp < 0) {
            // User already rated this app
            return;
        }

        Long current = System.currentTimeMillis()/1000;

        Long difference = current - rateYourApp;


        if(difference > (24 * 60 * 60) * 3) {
            //if(difference > 10) { // 10 seconds for testing app
            new AlertDialog.Builder(SketchPenActivity.this)
                    .setTitle(getString(R.string.alert_rate_title))
                    .setMessage(getString(R.string.alert_rate_body))
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    rate();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // do nothing
                                    Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, System.currentTimeMillis()/1000);
                                }
                            }).setIcon(android.R.drawable.ic_dialog_info).show();
        }

    }

    private void rate() {

        Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME,
                -1L);
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SketchPenActivity.this, "Unable to find market app", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void enableEraser() {

        int eraserSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);
        mPaint.setStrokeWidth(eraserSize == 0 ? Constants.DEFAULT_ERASER_SIZE : eraserSize);
        drawingView.setShowCircle(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setAntiAlias(true);
        eraserEnabled = true;
        Utils.showToast(SketchPenActivity.this, getString(R.string.label_message_eraser_on), Toast.LENGTH_SHORT);
        invalidateOptionsMenu();
    }

    private void disableEraser() {

        int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        int width = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int showCircle = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE);
        foreColor = foreColor == 0 ? Color.BLACK : foreColor;
        width = width == 0 ? Constants.DEFAULT_STROKE_SIZE : width;
        mPaint.setStrokeWidth(width);
        mPaint.setColor(foreColor);
        drawingView.setShowCircle(showCircle == 1);
        mPaint.setXfermode(null);
        eraserEnabled = false;
        invalidateOptionsMenu();
        Utils.showToast(SketchPenActivity.this, getString(R.string.label_message_eraser_off), Toast.LENGTH_SHORT);
    }

    private void startScan() {

        if (conn != null) {
            conn.disconnect();
            conn = null;
        }

        conn = new MediaScannerConnection(this, this);
        conn.connect();
    }

    private void insertImage() {

        int width = drawingView.getMeasuredWidth();
        int height = drawingView.getMeasuredHeight();

        Log.d(TAG, "width : " + width);
        Log.d(TAG, "height : " + height);

        //Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);

        try {
            // Code for crop image
            //Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .putExtra("crop", "true")
                    .putExtra("aspectX", width)
                    .putExtra("aspectY", height)
                    .putExtra("outputX", width)
                    .putExtra("outputY", height)
                    .putExtra("scale", true)
                    .putExtra("scaleUpIfNeeded", true)
                    .putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.action_insert_image)), SELECT_PICTURE);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_LONG);
        }

    }

    private void resetImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SketchPenActivity.this);
        builder.setTitle(getString(R.string.alert_reset_image_title));
        builder.setItems(new CharSequence[]
                        {getString(R.string.label_reset_foreground), getString(R.string.label_reset_background), getString(R.string.label_reset_all)},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                // Reset forground
                                drawingView.clear();
                                break;
                            case 1:
                                // Reset background
                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    drawingView.setBackgroundDrawable(null);
                                } else {
                                    drawingView.setBackground(null);
                                }

                                Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, 0);
                                int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
                                drawingView.setBackgroundColor(bgColor);
                                break;
                            case 2:
                                // Reset both
                                drawingView.clear();
                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    drawingView.setBackgroundDrawable(null);
                                } else {
                                    drawingView.setBackground(null);
                                }

                                Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, 0);
                                int backColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
                                drawingView.setBackgroundColor(backColor);
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {

                Bundle extras2 = data.getExtras();
                if (extras2 == null) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_external_storage_error), Toast.LENGTH_LONG);
                } else {

                    Bitmap photo = extras2.getParcelable("data");
                    int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        drawingView.setBackgroundDrawable(new BitmapDrawable(photo));
                    } else {
                        drawingView.setBackground(new BitmapDrawable(photo));
                    }
                }
            }
        }

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            flagAdFree = false;
            if (result.isFailure()) {
                // TODO : future
                if(result.getResponse() == -1005) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_canceled), Toast.LENGTH_LONG);

                } else if(result.getResponse() == -1008) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_refunded), Toast.LENGTH_LONG);
                }

                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }
        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                Utils.showToast(SketchPenActivity.this, result.getMessage(), Toast.LENGTH_LONG);
            } else {

                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            if (result.isSuccess()) {

                Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_done), Toast.LENGTH_LONG);
                Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED, 1);
                finish();
                startActivity(getIntent());
            } else {
                Utils.showToast(SketchPenActivity.this, result.getMessage(), Toast.LENGTH_LONG);
            }
        }
    };

    private void loadDrawingView() {

        drawingView = new DrawingView(SketchPenActivity.this);

        int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
        int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        int width = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int showCircle = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE);
        final Long showAd = Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);

        if(showAd == 0) {
            // Replacing 0 value with 1, app is launched already
            Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, 1L);
        }

        if(showAd != 0) {

            if(Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {

                SketchPenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // User launch app other than very first time
                        rateYourAppAutomatically();
                    }
                });
            }
        }

        bgColor = bgColor == 0 ? Color.WHITE : bgColor;
        foreColor = foreColor == 0 ? Color.BLACK : foreColor;
        width = width == 0 ? Constants.DEFAULT_STROKE_SIZE : width;

        drawingView.setBackgroundColor(bgColor);
        drawingView.setShowCircle(showCircle == 1);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(foreColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(width);
        drawingView.setCanvasPaint(mPaint);

        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        container.addView(drawingView);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjZM5vjXLYn/blsGe6QMxgSboBY8eGShG8ppUMTOCfl7XQMbQpIxkhpF+nKoiw2wp/ExVyfxycvgswfkb2sZet11whecvWx8Va672GIVXJSxLXHjYTVTy1mdmGTGO67C5E9k+tO2lklcZxxjEGQcfAUeh0v7pxf7iKk1J5SLKDoS0fOnyxbQ0JP8mg83TQBAUR6OB1GYCo/bYgnvH8izoCbW86kKiSoAWcZIO97lMm3+x85AcDzLQbw9QkRLQ95EOaLiUqS6zOOg+5ZGGWWstVS6VC6/XfO4QXnM9VMCVkdrdTHuc7IPlhOqNdgX8CjKhwN4F8qNHV/3wvUkWLlvSOQIDAQAB";

       mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });

        if(Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {

            SketchPenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadAd(showAd);
                }
            });
        }
    }

    private void loadAd(final Long showAd) {

        adView = new AdView(SketchPenActivity.this);
        adView.setAdUnitId("ca-app-pub-1782443214800184/3456231350");
        adView.setAdSize(AdSize.SMART_BANNER);
        // Create the interstitial.
        if (Utils.hasConnection(SketchPenActivity.this)) {
            // Initiate a generic request to load it with an ad
            AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice("91C131587FE98E6C1C9D95BFA28F01BD")
                        .build();
            adView.loadAd(adRequest);

            // Begin loading your interstitial.
            //interstitial.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    Utils.dLog("onAdClosed");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    Utils.dLog("onAdFailedToLoad");
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    Utils.dLog("onAdLeftApplication");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Utils.dLog("onAdOpened");
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Utils.dLog("onAdLoaded");
                    if (showAd != 0) {
                        // User launch app other than very first time
                        displayInterstitial();
                    }
                }
            });
        }

        FrameLayout  layout = (FrameLayout) drawingView.getParent();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layout.addView(adView, params);
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {

        interstitial = new InterstitialAd(SketchPenActivity.this);  //(SketchPenActivity.this, "a1530ed9c34caf8");
        interstitial.setAdUnitId("ca-app-pub-1782443214800184/5451818152");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("91C131587FE98E6C1C9D95BFA28F01BD")
                .build();
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitial.show();
            }
        });
    }

    @Override
    public void onMediaScannerConnected() {
        conn.scanFile(mediaScanFilePath, Constants.FILE_TYPE);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        if(progress != null) {
            progress.dismiss();
            progress = null;
        }
        try {
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivityForResult(intent, 0);
            }
        } finally {
            conn.disconnect();
            conn = null;
        }
    }

    private void buyAdFree() {

        if(flagAdFree) {
            return;
        }

        flagAdFree = true;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(adView != null) {
            adView.destroy();
        }

        interstitial = null;

        if (mHelper != null) mHelper.dispose();
        mHelper = null;

        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_close_app_title))
                .setMessage(getString(R.string.alert_close_app_body))
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                SketchPenActivity.super.onBackPressed();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                            }
                        }).setIcon(android.R.drawable.ic_dialog_info).show();
    }
}