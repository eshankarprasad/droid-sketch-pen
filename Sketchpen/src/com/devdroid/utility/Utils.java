package com.devdroid.utility;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

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
	
	public static void showToast(Context context, String message) {
		
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static String getRealPathFromURI(Context context, Uri contentUri) {

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
	}
	
	public static Point getScreenSize(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}
	
	public static int getActioBarHeight(Activity activity) {
		
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
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
}
