package com.devdroid.sketchpen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.DrawingView;
import com.devdroid.sketchpen.utility.Utils;
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
import java.io.IOException;

import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;

@SuppressLint("NewApi")
public class SketchPenActivity extends ActionBarActivity implements MediaScannerConnection.MediaScannerConnectionClient {

    private static final String TAG = SketchPenActivity.class.getSimpleName();
    // Test ITEM_SKU used for InAppPurchase the app
    //private static final String ITEM_SKU = "android.test.purchased";
    //private static final String ITEM_SKU = "android.test.cancelled";
    //private static final String ITEM_SKU = "android.test.refunded";
    //private static final String ITEM_SKU = "android.test.item_unavailable";

    // Production
    private static final String ITEM_SKU = "com.devdroid.sketchpen.adfree";
    private int backPressCount;
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
    private ImageView imageView;
    private Toolbar toolbar;
    private boolean isAnimating;
    private HorizontalScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sdk = android.os.Build.VERSION.SDK_INT;

        /*if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {

            // Enabling full screen mode
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }*/

        setContentView(R.layout.activity_sketch_pen);

        toolbar = (Toolbar) findViewById(R.id.toolbar_sketchpen);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mHelper = new IabHelper(this, Constants.BASE64_ENCODED_PUBLIC_KEY);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Utils.eLog("In-app Billing setup failed: " + result);
                } else {
                    Utils.dLog("In-app Billing is set up OK");
                }
            }
        });

        /*imageView = (ImageView) findViewById(R.id.imageview);
        imageView.setOnTouchListener(new OnSwipeTouchListener(SketchPenActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(SketchPenActivity.this, "top", Toast.LENGTH_SHORT).show();
                SketchPenActivity.this.getActionBar().show();
            }
            public void onSwipeRight() {
                Toast.makeText(SketchPenActivity.this, "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                Toast.makeText(SketchPenActivity.this, "left", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeBottom() {
                Toast.makeText(SketchPenActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                SketchPenActivity.this.getActionBar().hide();
            }
        });*/

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

            menu.getItem(1).setVisible(false); // "Color" will be hiden
            menu.getItem(3).setVisible(false); // "Enable eraser" will be hiden
            menu.getItem(4).setVisible(true);  // "Disable eraser" will be shown
        } else {
            menu.getItem(1).setVisible(true);  // "Color" will be shown
            menu.getItem(3).setVisible(true);  // "Enable eraser" will be shown
            menu.getItem(4).setVisible(false); // "Disable eraser" will be hidden
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
        } else if (item.getItemId() == R.id.action_stroke_size) {
            showStrokeSizeDialog(SketchPenActivity.this);
        } else if (item.getItemId() == R.id.action_view_images) {
            viewImages();
        } else if (item.getItemId() == R.id.action_save_image) {
            if (!"".equals(saveImage())) {
                Utils.showToast(SketchPenActivity.this, getString(R.string.toast_save_image_success), Toast.LENGTH_SHORT);
            }
        } else if (item.getItemId() == R.id.action_save_share) {
            saveImageAndShareWithFriend();
        } else if (item.getItemId() == R.id.action_insert_image) {
            insertImage();
        } else if (item.getItemId() == R.id.action_reset) {
            resetImage();
        } else if (item.getItemId() == R.id.action_rate) {
            rate();
        } else if (item.getItemId() == R.id.action_like) {
            like();
        } else if (item.getItemId() == R.id.action_enable_eraser) {
            enableEraser();
        } else if (item.getItemId() == R.id.action_disable_eraser) {
            disableEraser();
        } else if (item.getItemId() == R.id.action_buy) {
            if (Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {
                buyAdFree();
            } else {
                Utils.showToast(SketchPenActivity.this, "Already bought", Toast.LENGTH_LONG);
            }
        } else if (item.getItemId() == R.id.action_about) {
            PackageManager manager = this.getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                Utils.showToast(SketchPenActivity.this, Constants.DEBUG ? "Debug\n" : "Release\n" + "Version Code: " + info.versionCode + "\nVersion Name: " + info.versionName, Toast.LENGTH_SHORT);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.action_fullscreen) {

            if (isAnimating) return true;
            isAnimating = true;
            Utils.expandOrCollapse(SketchPenActivity.this, scrollView, "collapse");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        Utils.expandOrCollapse(SketchPenActivity.this, findViewById(R.id.card_show_toolbar), "expand");
                        isAnimating = false;
                    }
                }
            }, 500);
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToolbar(View view) {

        if (isAnimating) return;
        isAnimating = true;
        Utils.expandOrCollapse(SketchPenActivity.this, view, "collapse");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Utils.expandOrCollapse(SketchPenActivity.this, scrollView, "expand");
                    isAnimating = false;
                }
            }
        }, 500);
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
            }
        });
        dialogColorPicker.show();
    }

    private void showStrokeSizeDialog(Activity activity) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                SketchPenActivity.this.mPaint.setStrokeWidth(progress);
            }
        });

        int savedStrokeSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int savedEraserSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);

        if (eraserEnabled) {
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

        String folderPath = Environment.getExternalStorageDirectory().getPath() + "/sketchpen/";

        File folder = new File(folderPath);

        if (!folder.exists()) {
            Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found), Toast.LENGTH_LONG);
            return;
        }

        String[] allFiles = folder.list();

        if (allFiles.length > 0) {
            mediaScanFilePath = Environment.getExternalStorageDirectory().toString() + "/sketchpen/" + allFiles[allFiles.length - 1];
        } else {
            Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found), Toast.LENGTH_LONG);
            return;
        }
        if (allFiles.length > 0) {
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
        if (!myFile.exists()) {
            myFile.mkdir();
        }

        int newImageCount = Utils.getIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER) + 1;
        imagePath = myFile.getAbsolutePath() + "/sketch-pen00" + newImageCount + ".png";
        Utils.saveIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER, newImageCount);

        refreshGallery(imagePath);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            if (fos != null) {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }
        } catch (Exception e) {
            imagePath = "";
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.toast_save_image_error), Toast.LENGTH_SHORT).show();
            Utils.saveIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER, newImageCount - 1);
        }
        return imagePath;
    }

    private void refreshGallery(String imagePath) {

        try {
            // Start refreshing gallery
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imagePath);
            values.put(MediaStore.Images.Media.MIME_TYPE, Constants.FILE_TYPE);
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            // End refreshing gallery
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void saveImageAndShareWithFriend() {

        String imagePath = saveImage();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Constants.FILE_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imagePath));
        //intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_image_text));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.chooser_share_title)), Constants.REQUEST_SHARE_PICTURE);
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

        if (!Utils.hasConnection(SketchPenActivity.this)) {
            // No internet connectivity
            return;
        }

        if (rateYourApp < 0) {
            // User already rated this app
            return;
        }

        Long current = System.currentTimeMillis() / 1000;

        Long difference = current - rateYourApp;


        if (difference > (24 * 60 * 60) * 3) {
            //if(difference > 10) { // 10 seconds for testing app
            /*new AlertDialog.Builder(SketchPenActivity.this)
                    .setTitle(getString(R.string.alert_rate_title))
                    .setMessage(getString(R.string.alert_rate_body))
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // do nothing
                                    Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, System.currentTimeMillis()/1000);
                                }
                            }).setIcon(android.R.drawable.ic_dialog_info).show();*/
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            rate();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, System.currentTimeMillis() / 1000);
                            break;
                    }
                }
            };

            Utils.showAlert(SketchPenActivity.this, getString(R.string.alert_rate_title), getString(R.string.alert_rate_body), listener);
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
        getSupportActionBar().invalidateOptionsMenu();
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
        getSupportActionBar().invalidateOptionsMenu();
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

        File tempImageFile = new File(Utils.rootDirectoryPath(), Constants.TEMP_FILE_NAME);
        if (!tempImageFile.exists()) {
            try {
                tempImageFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        int width = drawingView.getMeasuredWidth();
        int height = drawingView.getMeasuredHeight();
        try {
            // Create intent to Open Image applications like Gallery, Google Photos
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra("crop", "true")
                    .putExtra("aspectX", width)
                    .putExtra("aspectY", height)
                    .putExtra("outputX", width)
                    .putExtra("outputY", height)
                    .putExtra("scale", true)
                    .putExtra("scaleUpIfNeeded", true)
                    .putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString())
                    .putExtra("return-data", false)
                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempImageFile));

            // Start the Intent
            startActivityForResult(intent, Constants.REQUEST_SELECT_PICTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == Constants.REQUEST_SELECT_PICTURE && data != null) {

                refreshGallery(Utils.rootDirectoryPath() + Constants.TEMP_FILE_NAME);
                try {
                    Bitmap photo = BitmapFactory.decodeFile(Utils.rootDirectoryPath() + Constants.TEMP_FILE_NAME);
                    if (photo == null) {
                        Uri uri = data.getData();
                        photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        Point p = Utils.getScreenSize(SketchPenActivity.this);
                        photo = Utils.getScaledBitmap(photo, p.x, p.y);
                    }
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        drawingView.setBackgroundDrawable(new BitmapDrawable(photo));
                    } else {
                        drawingView.setBackground(new BitmapDrawable(photo));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_LONG);
                }
            }
        }

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void resetImage() {

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
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
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            drawingView.setBackgroundDrawable(null);
                        } else {
                            drawingView.setBackground(null);
                        }

                        Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, Color.WHITE);
                        int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
                        drawingView.setBackgroundColor(bgColor);
                        break;
                    case 2:
                        // Reset both
                        drawingView.clear();
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            drawingView.setBackgroundDrawable(null);
                        } else {
                            drawingView.setBackground(null);
                        }

                        Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, Color.WHITE);
                        int backColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
                        drawingView.setBackgroundColor(backColor);
                        break;
                }
            }
        };
        Utils.showAlert(SketchPenActivity.this, listener, null, getString(R.string.label_reset_foreground), getString(R.string.label_reset_background), getString(R.string.label_reset_all));
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            flagAdFree = false;
            if (result.isFailure()) {
                // TODO : future
                if (result.getResponse() == -1005) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_canceled), Toast.LENGTH_LONG);

                } else if (result.getResponse() == -1008) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_refunded), Toast.LENGTH_LONG);
                }

                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }
        }
    };

    public void consumeItem() {
        if (mHelper == null) {

            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_SHORT);
        } else {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        }
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

        Utils.dLog("loadDrawingView");
        drawingView = new DrawingView(SketchPenActivity.this);

        int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
        int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        int width = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int showCircle = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE);
        final Long showAd = Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);

        if (showAd == 0) {
            // Replacing 0 value with 1, app is launched already
            Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, 1L);
        }

        if (showAd != 0) {

            if (Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {

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

        if (Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {

            SketchPenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadAd(showAd);
                    initInterstitial();
                }
            });
        }

        scrollView = (HorizontalScrollView) findViewById(R.id.scrollview);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    scrollView.smoothScrollTo(scrollView.getMaxScrollAmount(), 0);
                    Utils.dLog(scrollView.getMaxScrollAmount() + " :Max Scroll Amount");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()) {
                                scrollView.smoothScrollTo(0, 0);
                                Utils.dLog(scrollView.getMaxScrollAmount() + " :Max Scroll Amount");
                            }
                        }
                    }, 500);
                }
            }
        }, 1000);
    }

    private void loadAd(final Long showAd) {

        adView = new AdView(SketchPenActivity.this);
        adView.setAdUnitId(Constants.AD_UNIT_ID_BANNER);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAlpha(Constants.ALPHA_LEVEL);
        // Create the interstitial.
        if (Utils.hasConnection(SketchPenActivity.this)) {
            // Initiate a generic request to load it with an ad
            AdRequest adRequest = Utils.newAdRequestInstance();
            adView.loadAd(adRequest);
        }

        FrameLayout layout = (FrameLayout) drawingView.getParent();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layout.addView(adView, params);
    }

    // Invoke initInterstitial() when you are ready to display an interstitial.
    public void initInterstitial() {

        interstitial = new InterstitialAd(SketchPenActivity.this);  //(SketchPenActivity.this, "a1530ed9c34caf8");
        interstitial.setAdUnitId(Constants.AD_UNIT_ID_INTERSTITIAL);
        AdRequest adRequest = Utils.newAdRequestInstance();
        interstitial.loadAd(adRequest);
    }

    @Override
    public void onMediaScannerConnected() {
        conn.scanFile(mediaScanFilePath, Constants.FILE_TYPE);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
        try {
            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivityForResult(intent, Constants.REQUEST_VIEW_PICTURE);
            }
        } finally {
            conn.disconnect();
            conn = null;
        }
    }

    private void buyAdFree() {

        if (flagAdFree) {
            return;
        }

        flagAdFree = true;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
        if (mHelper != null) mHelper.dispose();
    }

    @Override
    public void onBackPressed() {

        backPressCount++;

        if (backPressCount > 1) {
            if (interstitial != null) {
                interstitial.show();
            }
            super.onBackPressed();
        } else {
            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_close_app), Toast.LENGTH_SHORT);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        backPressCount = 0;
                    }
                }
            }, Constants.DURATION_EXIT_APP);
        }
    }
}