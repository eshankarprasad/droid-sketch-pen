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
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.DrawingView;
import com.devdroid.sketchpen.utility.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;*/

@SuppressLint("NewApi")
public class SketchPenActivity extends AppCompatActivity implements MediaScannerConnection.MediaScannerConnectionClient,
        View.OnClickListener, View.OnLongClickListener, Toolbar.OnMenuItemClickListener {

    private static final String TAG = SketchPenActivity.class.getSimpleName();
    // Test ITEM_SKU used for InAppPurchase the app
    //private static final String ITEM_SKU = "android.test.purchased";
    //private static final String ITEM_SKU = "android.test.cancelled";
    //private static final String ITEM_SKU = "android.test.refunded";
    //private static final String ITEM_SKU = "android.test.item_unavailable";

    // Production
    //private static final String ITEM_SKU = "com.devdroid.sketchpen.adfree";
    LinearLayout btnLoginToLike;
    LikeView likeView;
    CallbackManager callbackManager;
    /*IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
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
    };*/
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
    //private IabHelper mHelper;
    /*IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
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
    };*/
    private boolean isAnimating;
    private HorizontalScrollView scrollView;
    private AdRequest request;

    public static Intent getIntent(Activity activity) {
        Intent intent = new Intent(activity, SketchPenActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sdk = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_sketch_pen);
        FacebookSdk.sdkInitialize(getApplicationContext());

        /*mHelper = new IabHelper(this, Constants.BASE64_ENCODED_PUBLIC_KEY);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Utils.eLog("In-app Billing setup failed: " + result);
                } else {
                    Utils.dLog("In-app Billing is set up OK");
                }
            }
        });*/

        initInstances();

        initCallbackManager();
        refreshButtonsState();

        loadDrawingView();
        //logKeyHash();
    }

    private void logKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.devdroid.sketchpen", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Utils.eLog("KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    private void initInstances() {

        btnLoginToLike = (LinearLayout) findViewById(R.id.btnLoginToLike);
        likeView = (LikeView) findViewById(R.id.likeView);
        likeView.setLikeViewStyle(LikeView.Style.STANDARD);
        likeView.setAuxiliaryViewPosition(LikeView.AuxiliaryViewPosition.INLINE);
        likeView.setObjectIdAndType(Constants.FACEBOOK_PAGE_URL, LikeView.ObjectType.OPEN_GRAPH);

        btnLoginToLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(SketchPenActivity.this, Arrays.asList("public_profile"));
            }
        });
    }

    private void initCallbackManager() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                refreshButtonsState();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
    }

    private void refreshButtonsState() {
        if (!isLoggedIn()) {
            btnLoginToLike.setVisibility(View.VISIBLE);
            likeView.setVisibility(View.GONE);
        } else {
            btnLoginToLike.setVisibility(View.GONE);
            likeView.setVisibility(View.VISIBLE);
        }
    }

    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null;
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

    private void showStrokeColorDialog(boolean isBackgroundColor) {
        Intent intent = new Intent(this, ChooseColorActivity.class);
        intent.putExtra(Constants.KEY_BG_COLOR, isBackgroundColor);
        startActivityForResult(intent, Constants.REQUEST_CHOOSE_COLOR);
        Utils.animateActivity(this, "up");
    }

    private void showStrokeColorDialog(SketchPenActivity activity) {

        int foreColor = Color.BLACK;

        try {
            foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            foreColor = (int) Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        }


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

    private void showStrokeSizeDialog(boolean enableEraser) {
        Intent intent = new Intent(this, StrokeSizeActivity.class);
        intent.putExtra(Constants.KEY_ERASER_ENABLE_DISABLE, enableEraser);
        startActivityForResult(intent, Constants.REQUEST_STROKE_SIZE);
        Utils.animateActivity(this, "up");
    }

    private void showStrokeSizeDialog(Activity activity) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_stroke_size);

        NativeExpressAdView adView = (NativeExpressAdView) dialog.findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Utils.dLog("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Utils.dLog("onAdFailedToLoad");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Utils.dLog("onAdLoaded");
            }
        });
        adView.loadAd(request);

        TextView textViewTitle = (TextView) dialog.findViewById(R.id.textview_size_label);
        if (eraserEnabled) {
            textViewTitle.setText(getString(R.string.label_eraser_size));
        } else {
            textViewTitle.setText(getString(R.string.label_stroke_size));
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

        dialog.show();
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

        imagePath = myFile.getAbsolutePath() + "/sketchpen_" + System.currentTimeMillis() + ".png";
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

        long rateYourApp = Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);

        if (!Utils.hasConnection(SketchPenActivity.this)) {
            // No internet connectivity
            return;
        }

        if (rateYourApp < 0) {
            // User already rated this app
            return;
        }

        long current = System.currentTimeMillis() / 1000;

        long difference = current - rateYourApp;

        if (difference > (24 * 60 * 60) * 3) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            rate();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            Utils.saveLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, System.currentTimeMillis() / 1000);
                            break;
                    }
                }
            };

            Utils.showAlert(SketchPenActivity.this, getString(R.string.alert_rate_title), getString(R.string.alert_rate_body), listener);
        }

    }

    private void rate() {

        Utils.saveLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, -1L);
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SketchPenActivity.this, "Unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private void enableEraser() {

        int eraserSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);
        mPaint.setStrokeWidth(eraserSize == 0 ? Constants.DEFAULT_ERASER_SIZE : eraserSize);
        drawingView.setShowCircle(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setAntiAlias(true);
        eraserEnabled = true;
        Utils.showDroidMessage(SketchPenActivity.this, getString(R.string.label_message_eraser_on), Toast.LENGTH_SHORT);
        toggleEraser();
    }

    private void toggleEraser() {
        ImageView imgEraser = (ImageView) findViewById(R.id.img_eraser);
        TextView txtEraser = (TextView) findViewById(R.id.txt_eraser);
        View btnEraser = findViewById(R.id.btn_eraser);
        View btnChangeColor = findViewById(R.id.btn_color);
        TextView txtStrokeSize = (TextView) findViewById(R.id.txt_stroke_size);
        //TransitionDrawable transition = (TransitionDrawable) btnEraser.getBackground();
        if (eraserEnabled) {
            imgEraser.setImageResource(R.drawable.ic_action_eraser_state_on);
            txtEraser.setText(R.string.label_message_eraser_on);
            btnEraser.setTag(getString(R.string.label_message_eraser_on));
            txtStrokeSize.setText(R.string.label_eraser_size);
            btnChangeColor.setVisibility(View.GONE);
            //transition.startTransition(1500);
        } else {
            imgEraser.setImageResource(R.drawable.ic_action_eraser_state_off);
            txtEraser.setText(R.string.label_message_eraser_off);
            btnEraser.setTag(getString(R.string.label_message_eraser_off));
            txtStrokeSize.setText(R.string.label_stroke_size);
            btnChangeColor.setVisibility(View.VISIBLE);
            //transition.reverseTransition(1500);
        }
    }

    private void disableEraser(boolean displayMessage) {

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
        toggleEraser();
        if (displayMessage) {
            Utils.showDroidMessage(SketchPenActivity.this, getString(R.string.label_message_eraser_off), Toast.LENGTH_SHORT);
        }
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
        Crop.pickImage(SketchPenActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            try {
                handleCrop(resultCode, result);
            } catch (IOException e) {
                e.printStackTrace();
                Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_LONG);
            }
        } else if (requestCode == Constants.REQUEST_TUTORIAL && resultCode == RESULT_OK) {
            Utils.saveBooleanPreferences(SketchPenActivity.this, Constants.KEY_TUTORIAL_SHOWN, true);

            // Replacing 0 value with 1, app is launched already
            Utils.saveLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, 1L);
            Utils.showToast(SketchPenActivity.this, getString(R.string.title_welldone), Toast.LENGTH_SHORT);
        } else if (requestCode == Constants.REQUEST_STROKE_SIZE && resultCode == RESULT_OK) {
            mPaint.setStrokeWidth(Utils.getIntegerPreferences(this, eraserEnabled ? Constants.KEY_ERASER_SIZE : Constants.KEY_STROKE_SIZE));
        } else if (requestCode == Constants.REQUEST_CHOOSE_COLOR && resultCode == RESULT_OK) {
            boolean isBackgroundColor = result.getBooleanExtra(Constants.KEY_BG_COLOR, false);
            int color = isBackgroundColor ? Color.WHITE : Color.BLACK;
            try {
                color = Utils.getIntegerPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
            } catch (ClassCastException e) {
                e.printStackTrace();
                color = (int) Utils.getLongPreferences(this, isBackgroundColor ? Constants.KEY_BG_COLOR : Constants.KEY_FORE_COLOR);
            }

            if (isBackgroundColor) {
                drawingView.setBackgroundColor(color);
                findViewById(R.id.img_bg_color).setBackgroundColor(color);
            } else {
                mPaint.setColor(color);
                findViewById(R.id.img_color).setBackgroundColor(color);
            }

            /*Bitmap sourceBitmap = Utils.convertDrawableToBitmap(getDrawable(R.drawable.ic_action_back_color));
            Bitmap mFinalBitmap = Utils.changeImageColor(sourceBitmap, color);
            ImageView imageView = (ImageView) findViewById(R.id.img_bg_color);
            imageView.setImageDrawable(null);
            imageView.setImageBitmap(mFinalBitmap);*/
        }

        /*if (!mHelper.handleActivityResult(requestCode, resultCode, result)) {
            super.onActivityResult(requestCode, resultCode, result);
        }*/

        callbackManager.onActivityResult(requestCode, resultCode, result);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Point origin = Utils.getScreenSize(SketchPenActivity.this);
        Crop.of(source, destination).withAspect(origin.x, origin.y).start(this);
        //Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) throws IOException {
        if (resultCode == RESULT_OK) {
            Uri uri = Crop.getOutput(result);
            //Bitmap  mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            //drawingView.setBackgroundBitmap(mBitmap);
            loadBackground(uri);
        } else if (resultCode == Crop.RESULT_ERROR) {
            //Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
            Utils.showToast(SketchPenActivity.this, Crop.getError(result).getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void loadBackground(Uri uri) {
        try {
            Bitmap myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Utils.dLog("Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            } catch (Exception e) {

            }

            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                drawingView.setBackgroundDrawable(new BitmapDrawable(myBitmap));
            } else {
                drawingView.setBackground(new BitmapDrawable(myBitmap));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_LONG);
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

    /*public void consumeItem() {
        if (mHelper == null) {

            Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_SHORT);
        } else {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        }
    }*/

    private void loadDrawingView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_sketch_pen);
        toolbar.setOnMenuItemClickListener(this);

        initLongClickableViews();

        Utils.dLog("loadDrawingView");
        drawingView = new DrawingView(SketchPenActivity.this);

        int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
        int foreColor = Color.BLACK;
        try {
            foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            foreColor = (int) Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
        }

        int width = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
        int showCircle = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE);
        final long showAd = Utils.getLongPreferences(SketchPenActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);

        bgColor = bgColor == 0 ? Color.WHITE : bgColor;
        foreColor = foreColor == 0 ? Color.BLACK : foreColor;
        width = width == 0 ? Constants.DEFAULT_STROKE_SIZE : width;

        findViewById(R.id.img_bg_color).setBackgroundColor(bgColor);
        findViewById(R.id.img_color).setBackgroundColor(foreColor);

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

            if (!isFinishing()) {
                loadAd(showAd);
                initInterstitial();
            }
        }

        scrollView = (HorizontalScrollView) findViewById(R.id.scrollview);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    scrollView.smoothScrollTo(scrollView.getMaxScrollAmount(), 0);
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

        if (!Utils.getBooleanPreferences(SketchPenActivity.this, Constants.KEY_TUTORIAL_SHOWN)) {
            startActivityForResult(TutorialActivity.getIntent(SketchPenActivity.this), Constants.REQUEST_TUTORIAL);
        } else {
            // Rating dialog to show
            if (showAd != 0) {

                if (Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {

                    if (!isFinishing()) {
                        // User launch app other than very first time
                        rateYourAppAutomatically();
                    }
                }
            }
        }
    }

    private void initLongClickableViews() {
        findViewById(R.id.btn_fullscreen).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_color).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_bg_color).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_stroke_size).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_eraser).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_view_image).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_insert_image).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_save_image).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_share).setOnLongClickListener(SketchPenActivity.this);
        findViewById(R.id.btn_reset).setOnLongClickListener(SketchPenActivity.this);
    }

    private void loadAd(final long showAd) {

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

    /*private void buyAdFree() {

        if (flagAdFree) {
            return;
        }

        flagAdFree = true;
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }*/

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
        //if (mHelper != null) mHelper.dispose();
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

    /*private void showRestOptionDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Rate on Play Store
                        rate();
                        break;
                    case 1: // Visit page
                        like();
                        break;
                    case 2: // About Us
                        aboutUs();
                }
            }
        };
        Utils.showAlert(SketchPenActivity.this, listener, null,
                getString(R.string.action_visit_sketchpen),
                getString(R.string.action_rate),
                getString(R.string.action_buy_ad_free),
                getString(R.string.action_about));
    }*/

    private void aboutUs() {
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            Utils.showDroidMessage(SketchPenActivity.this, BuildConfig.DEBUG ? "Debug\n" : "Release\n" + "Version Code: " + info.versionCode + "\nVersion Name: " + info.versionName, Toast.LENGTH_SHORT);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doFullScreen() {
        if (isAnimating) return;
        isAnimating = true;
        Utils.expandOrCollapse(SketchPenActivity.this, scrollView, "collapse");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Utils.expandOrCollapse(SketchPenActivity.this, findViewById(R.id.card_show_toolbar), "expand");
                    if (eraserEnabled) {
                        Utils.showDroidMessage(SketchPenActivity.this, getString(R.string.label_message_fullscreen_eraser_on), Toast.LENGTH_SHORT);
                        disableEraser(false);
                    }
                    isAnimating = false;
                }
            }
        }, 500);
    }

    @Override
    public void onClick(View view) {
        view.setEnabled(false);
        switch (view.getId()) {
            case R.id.btn_fullscreen:
                doFullScreen();
                break;
            case R.id.btn_color:
                showStrokeColorDialog(false);
                break;
            case R.id.btn_bg_color:
                showStrokeColorDialog(true);
                break;
            case R.id.btn_stroke_size:
                //showStrokeSizeDialog(SketchPenActivity.this);
                showStrokeSizeDialog(eraserEnabled);
                break;
            case R.id.btn_eraser:
                if (eraserEnabled) {
                    disableEraser(true);
                } else {
                    enableEraser();
                }
                break;
            case R.id.btn_view_image:
                viewImages();
                break;
            case R.id.btn_insert_image:
                insertImage();
                break;
            case R.id.btn_save_image:
                if ("".equals(saveImage())) {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error), Toast.LENGTH_SHORT);
                } else {
                    Utils.showToast(SketchPenActivity.this, getString(R.string.toast_save_image_success), Toast.LENGTH_SHORT);
                }
                break;
            case R.id.btn_share:
                saveImageAndShareWithFriend();
                break;
            case R.id.btn_reset:
                resetImage();
                break;
            /*case R.id.btn_more:
                showRestOptionDialog();
                break;*/
            /*case R.id.btn_undo:
                drawingView.undo();
                break;*/
        }
        view.setEnabled(true);
    }

    @Override
    public boolean onLongClick(View view) {
        Utils.showToast(SketchPenActivity.this, (String) view.getTag(), Toast.LENGTH_SHORT);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_rate: // Rate on Play Store
                rate();
                break;
            case R.id.action_visit: // Visit page
                like();
                break;
            case R.id.action_about: // About Us
                aboutUs();
                break;
        }
        return false;
    }
}