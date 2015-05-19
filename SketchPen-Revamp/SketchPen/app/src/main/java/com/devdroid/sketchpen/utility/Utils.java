package com.devdroid.sketchpen.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.sketchpen.R;
import com.google.android.gms.ads.AdRequest;

import java.io.File;

public class Utils {

    public static void saveIntegerPreferences(Context context, String key,
                                              Long value) {
        SharedPreferences sPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void saveIntegerPreferences(Context context, String key,
                                              Integer value) {
        SharedPreferences sPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static Long getLongPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Long savedPref = sharedPreferences.getLong(key, 0);
        return savedPref;
    }

    public static Integer getIntegerPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Integer savedPref = sharedPreferences.getInt(key, 0);
        return savedPref;
    }

    // Check for Internet connectivity
    public static boolean hasConnection(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public static void showToast(Context context, String message, int timeout) {

        Toast toast = Toast.makeText(context, message, timeout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(30);
        toastTV.setGravity(Gravity.CENTER);
        toast.show();
    }

	/*public static String getRealPathFromURI(Context context, Uri contentUri) {

		String[] proj = { MediaStore.Images.Media.DATA };

		// This method was deprecated in API level 11
		// Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj,
				null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}*/

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getScreenSize(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            int width = display.getWidth();
            int height = display.getHeight();

            size.set(width, height);
        } else {
            display.getSize(size);
        }
        return size;
    }

    public static int getActioBarHeight(Activity activity) {

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }

    public static int getStatusBarHeight(Activity activity) {

        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void eLog(String string) {
        if (Constants.DEBUG) {
            Log.e(Constants.TAG, string);
        }
    }

    public static void dLog(String string) {
        if (Constants.DEBUG) {
            Log.d(Constants.TAG, string);
        }
    }

    public static AdRequest newAdRequestInstance() {

        AdRequest adRequest = null;
        if (Constants.DEBUG) {
            adRequest = new AdRequest.Builder().addTestDevice(Constants.AD_TEST_DEVICE).build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        return adRequest;
    }

    /**
     * Display alert with single option button
     * @param activity
     * @param title
     * @param message
     */
    public static void showAlert(Activity activity, String title, String message) {

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing here; it will close the dialog self
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    /**
     * Display alert with two option buttons i.e. yes and no
     * @param activity
     * @param title
     * @param message
     * @param listener
     */
    public static void showAlert(Activity activity, String title, String message, DialogInterface.OnClickListener listener) {

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, listener)
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    /**
     * Display alert with multiple option buttons
     * @param activity
     * @param listener
     * @param title
     * @param options
     */
    public static void showAlert(Activity activity, DialogInterface.OnClickListener listener, String title, CharSequence... options) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null && title.equals("")) {
            builder.setTitle(title);
        }
        builder.setItems(options, listener);
        builder.create().show();
    }

    public static String rootDirectoryPath() {

        File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/sketchpen");
        if(!myFile.exists()) {
            myFile.mkdir();
        }

        return myFile.getAbsolutePath();
    }
}
