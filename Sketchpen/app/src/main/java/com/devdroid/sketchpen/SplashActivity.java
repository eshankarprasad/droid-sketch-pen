package com.devdroid.sketchpen;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.devdroid.sketchpen.cloud.AppInfo;
import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.DialogInterface.OnClickListener;


public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private long timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        timeStamp = System.currentTimeMillis();
        checkUpcomingAppVersion();

        //checkPermissionAndLaunchHome();
    }

    private void checkUpcomingAppVersion() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("sketchpen_db").document("app_metadata");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                AppInfo appInfo = documentSnapshot.toObject(AppInfo.class);
                Utils.dLog("***************************" + appInfo.toString());

                if (appInfo.getStoreVersion() > BuildConfig.VERSION_CODE) {
                    // Update available
                    if (appInfo.isForceUpdate()) {
                        // Force update
                        showUpdateAlert(false, appInfo);
                    } else {
                        if (appInfo.getOutdatedVersion() > BuildConfig.VERSION_CODE) {
                            // Force update
                            showUpdateAlert(false, appInfo);
                        } else {
                            // Soft update
                            showUpdateAlert(true, appInfo);
                        }
                    }
                } else {
                    // Update not available
                    checkPermissionAndLaunchHome();
                }
            }
        });
    }

    private void showUpdateAlert(boolean canSkip, AppInfo appInfo) {
        if (canSkip) {
            Utils.showAlert(SplashActivity.this, "Update Found", appInfo.getMessage(), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Redirect to play store
                            redirectToPlayStore();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // Exit app
                            checkPermissionAndLaunchHome();
                            break;
                    }
                }
            });
        } else {
            Utils.showAlert(SplashActivity.this, "Update Required", appInfo.getMessage() + Constants.CANCEL_WARNING, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Redirect to play store
                            redirectToPlayStore();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // Exit app
                            SplashActivity.this.finish();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
        Utils.animateActivity(this, "down");
    }

    private void redirectToPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to find market app", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void checkPermissionAndLaunchHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_WRITE_STORAGE);
        } else {
            launchLandingPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                launchLandingPage();
            } else {
                Toast.makeText(this, "Until you grant the permission, you can'nt launch the app", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void launchLandingPage() {
        if (System.currentTimeMillis() - timeStamp > 1500) {
            if (!isFinishing()) {
                finish();
                startActivity(SketchPenActivity.getIntent(SplashActivity.this));
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        finish();
                        startActivity(SketchPenActivity.getIntent(SplashActivity.this));
                    }
                }
            }, 1500);
        }
    }
}
