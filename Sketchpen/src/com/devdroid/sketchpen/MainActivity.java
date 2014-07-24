package com.devdroid.sketchpen;

import info.androidhive.slidingmenu.adapter.NavDrawerListAdapter;
import info.androidhive.slidingmenu.model.NavDrawerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.devdroid.utility.Constants;
import com.devdroid.utility.DrawingView;
import com.devdroid.utility.Utils;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;
import com.google.ads.InterstitialAd;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements MediaScannerConnectionClient {
	
	private static final String TAG = "SketchPen";
	private static final int SELECT_PICTURE = 1;
	private String mediaScanFilePath;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
	private DrawingView drawingView;
	private Paint mPaint;
	private AdView adView;
	private InterstitialAd interstitial;
	private ProgressDialog progress;
	private MediaScannerConnection conn;
	private boolean eraserEnabled;
	private int sdk;

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
		
		setContentView(R.layout.activity_main);
		
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		
		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		
		// Settings
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		
		// View images
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		
		// Save imag
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		
		// Save and share
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		
		// Insert image
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
		
		// Eraser
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, getResources().getString(R.string.eraser_inactive)));
		
		// Erase all
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
		
		// Rating and review
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
		
		// Like us
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			
			// enabling action bar app icon and behaving it as toggle button
			getActionBar().setDisplayHomeAsUpEnabled(true);
			
			if(sdk >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				// This code will work only with Ice Cream Sandwitch or above version
				getActionBar().setHomeButtonEnabled(true);
			}
		
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					R.drawable.ic_drawer, // nav menu toggle icon
					R.string.app_name, // nav drawer open - description for
										// accessibility
					R.string.app_name // nav drawer close - description for
										// accessibility
			) {
				public void onDrawerClosed(View view) {
					getActionBar().setTitle(mTitle);
					// calling onPrepareOptionsMenu() to show action bar icons
					invalidateOptionsMenu();
				}
	
				public void onDrawerOpened(View drawerView) {
					getActionBar().setTitle(mDrawerTitle);
					// calling onPrepareOptionsMenu() to hide action bar icons
					invalidateOptionsMenu();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}
		loadDrawingView();
	}

	

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			performAction(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// toggle nav drawer on selecting action bar app icon/title
		if (sdk >= android.os.Build.VERSION_CODES.HONEYCOMB && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		performAction(item.getOrder());
		
		return super.onOptionsItemSelected(item);
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		//boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			for (int i = 0; i < menu.size(); i++) {
				menu.getItem(i).setVisible(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void performAction(final int position) {
		// update the main content by replacing fragments
		//Fragment fragment = null;
		switch (position) {
		case 0:
			// Settings
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Setting dialog will be shown after 0.3 second 
	                	   viewSettingDialog();
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 1:
			// View images
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Images will be shown after 0.3 second 
	                	   viewImages();
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 2:
			// Save images
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Images will be shaved after 0.3 second 
	                	   if(!"".equals(saveImage())) {
	                		   Utils.showToast(MainActivity.this, getResources().getString(R.string.toast_save_image_success));
	                       }
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 3:
			// Save and share
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Images will be shaved and share after 0.3 second 
	                	   saveImageAndShareWithFriend();
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 4:
			// Insert image
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Images will be inserted after 0.3 second 
	                	   insertImage();
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 5:
			// Eraser
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Eraser will be active after 0.2 second 
	                	   if(getResources().getString(R.string.eraser_active).equals(navDrawerItems.get(position).getCount())) {
	                		   navDrawerItems.get(position).setCount(getResources().getString(R.string.eraser_inactive));
	                		   int foreColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_FORE_COLOR);
	                		   int width = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_STROKE_SIZE);
	                		   int showCircle = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_SHOW_CIRCLE);
	                		   foreColor = foreColor == 0 ? Color.BLACK : foreColor;
	                		   width = width == 0 ? 12 : width;
	                		   mPaint.setStrokeWidth(width);
	                		   mPaint.setColor(foreColor);
	                		   drawingView.setShowCircle(showCircle == 1);
	                		   mPaint.setXfermode(null);
	                		   eraserEnabled = false;
	                		   
	                	   } else {
	                		   navDrawerItems.get(position).setCount(getResources().getString(R.string.eraser_active));
	                		   int eraserSize = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_ERASER_SIZE);
	                		   mPaint.setStrokeWidth(eraserSize);
	                		   drawingView.setShowCircle(true);
	                	       mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	                	       mPaint.setAntiAlias(true);
	                	       eraserEnabled = true;
	                	       Utils.showToast(MainActivity.this, getResources().getString(R.string.label_message_eraser));
	                	   }
	                   }
	                 }, 0);
	            }
	        });
			
			break;
		
		case 6:
			// Erase all
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Images will be inserted after 0.3 second 
	                	   resetImage();
	                   }
	                 }, 300);
	            }
	        });
			
			break;
		case 7:
			// Rating and review
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Rating window will be opened after 0.3 second 
	                	   rate();
	                   }
	                 }, 300);
	            }
	        });
			break;
			
		case 8:
			// Like us
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                 final Handler handler = new Handler();
	                 handler.postDelayed(new Runnable() {
	                   @Override
	                   public void run() {
	                	   // Rating window will be opened after 0.3 second 
	                	   like();
	                   }
	                 }, 300);
	            }
	        });
			break;
		default:
			break;
		}

		
		//mDrawerList.setSelection(position);
		//setTitle(navMenuTitles[position]);
		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		
		runOnUiThread(new Runnable() {

            @Override
            public void run() {
                 final Handler handler = new Handler();
                 handler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                	   if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                		   // item will be unselected after 0.5 second 
                		   mDrawerList.setItemChecked(position, false);
                	   }
                   }
                 }, 500);
            }
        });
		
		
		//FrameLayout  fragment = (FrameLayout) drawingView.getParent();
		/*if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}*/
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setTitle(mTitle);
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		if(sdk >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}
	
	private void loadDrawingView() {
		
		drawingView = new DrawingView(MainActivity.this);
		
		int bgColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_BG_COLOR);
		int foreColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_FORE_COLOR);
		int width = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_STROKE_SIZE);
		int showCircle = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_SHOW_CIRCLE);
		final Long showAd = Utils.getLongPreferences(MainActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);
		
		if(showAd == 0) {
			// Replacing 0 value with 1, app is launched already
			Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, 1L);
		}
		
		if(showAd != 0) {
			// User launch app other than very first time
			rateYourApp();
		}
		
		bgColor = bgColor == 0 ? Color.WHITE : bgColor;
		foreColor = foreColor == 0 ? Color.BLACK : foreColor;
		width = width == 0 ? 12 : width;
		
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
		loadAd(showAd);
	}

	private void rateYourApp() {
		
		Long rateYourApp = Utils.getLongPreferences(MainActivity.this, Constants.KEY_RATE_YOUR_APP_TIME);
		
		if(!Utils.hasConnection(MainActivity.this)) {
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
			new AlertDialog.Builder(MainActivity.this)
				.setTitle(getResources().getString(R.string.alert_rate_title))
				.setMessage(getResources().getString(R.string.alert_rate_body))
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
							Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_RATE_YOUR_APP_TIME, System.currentTimeMillis()/1000);
						}
					}).setIcon(android.R.drawable.ic_dialog_info).show();
		}
		
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
	
	private void rate() {
	
		Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_RATE_YOUR_APP_TIME,
				-1L);
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(myAppLinkToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(MainActivity.this, "Unable to find market app", Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private void loadAd(final Long showAd) {
		  
		adView = new AdView((Activity) MainActivity.this, com.google.ads.AdSize.BANNER, "a1530ed9c34caf8");
	    // Create the interstitial.
	    interstitial = new InterstitialAd((Activity) MainActivity.this, "a1530ed9c34caf8");
	    
		if (Utils.hasConnection(MainActivity.this)) {
			// Initiate a generic request to load it with an ad
			AdRequest adRequest = new AdRequest();
			adView.loadAd(adRequest);

		    // Begin loading your interstitial.
		    interstitial.loadAd(adRequest);
			adView.setAdListener(new AdListener() {

				public void onReceiveAd(Ad arg0) {
					adView.setVisibility(View.VISIBLE);
					Log.d(TAG, "onReceiveAd");
					if (showAd != 0) {
						// User launch app other than very first time
						displayInterstitial();
					}
				}

				public void onPresentScreen(Ad arg0) {

				}

				public void onLeaveApplication(Ad arg0) {

				}

				public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
					Log.d(TAG, "onFailedToReceiveAd");
					adView.setVisibility(View.GONE);
				}

				public void onDismissScreen(Ad arg0) {

				}
			});
		}
		
		FrameLayout container = (FrameLayout) findViewById(R.id.frame_container);
		container.addView(drawingView);
		
		FrameLayout  layout = (FrameLayout) drawingView.getParent();		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		layout.addView(adView, params);
	}

	// Invoke displayInterstitial() when you are ready to display an interstitial.
	public void displayInterstitial() {
	
		if (interstitial.isReady()) {
			interstitial.show();
		}
	}
	
	private void viewSettingDialog() {
		
		final Dialog dialog = new Dialog(MainActivity.this);
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(getResources().getString(R.string.action_settings));
		dialog.setContentView(R.layout.dialog_setting);
		dialog.show();
		
		int bgColor = Utils.getIntegerPreferences(this, Constants.KEY_BG_COLOR);
		int foreColor = Utils.getIntegerPreferences(this, Constants.KEY_FORE_COLOR);
		final int size = Utils.getIntegerPreferences(this, Constants.KEY_STROKE_SIZE);
		final int eSize = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_ERASER_SIZE);
		int showCircle = Utils.getIntegerPreferences(this, Constants.KEY_SHOW_CIRCLE);
		
		final Button buttonShowCircle = (Button) dialog.findViewById(R.id.button_show_circle);
		buttonShowCircle.setText(showCircle == 1 ? "ON" : "OFF");
		
		buttonShowCircle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				buttonShowCircle.setText("ON".equals(buttonShowCircle.getText().toString()) ? "OFF" : "ON");
			}
		});
		
		final Button buttonForeColor = (Button) dialog.findViewById(R.id.button_forecolor);
		
		if(foreColor == 0) {
			buttonForeColor.setBackgroundColor(Color.BLACK);
			buttonForeColor.setTag(Color.BLACK);
		} else {
			buttonForeColor.setBackgroundColor(foreColor);
			buttonForeColor.setTag(foreColor);
		}
		
		buttonForeColor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int foreColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_FORE_COLOR);
				foreColor = foreColor == 0 ? foreColor = Color.BLACK : foreColor;
				
				final Dialog dialogColorPicker = new Dialog(MainActivity.this);
				dialogColorPicker.setTitle(getResources().getString(R.string.label_forecolor));
				dialogColorPicker.setContentView(R.layout.dialog_color_picker);
				
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
				picker.setOldCenterColor(foreColor);
				picker.setNewCenterColor(foreColor);
				
				Log.d(TAG, buttonForeColor.getTag() + " buttonForeColor.getTag()");
				
				//to turn of showing the old color
				picker.setShowOldCenterColor(true);
				
				dialogColorPicker.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						
						Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_FORE_COLOR, picker.getColor());
						MainActivity.this.mPaint.setColor(picker.getColor());
						buttonForeColor.setBackgroundColor(picker.getColor());
						Log.d(TAG, "picker.getColor():" + picker.getColor());
					}
				});
				
				dialogColorPicker.show();
			}
		});
		
		final Button buttonBackColor = (Button) dialog.findViewById(R.id.button_backcolor);
		if(bgColor == 0) {
			buttonBackColor.setBackgroundColor(Color.WHITE);
			buttonBackColor.setTag(Color.WHITE);
		} else {
			buttonBackColor.setBackgroundColor(bgColor);
			buttonBackColor.setTag(bgColor);
		}
		buttonBackColor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int bgColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_BG_COLOR);
				bgColor = bgColor == 0 ? bgColor = Color.WHITE : bgColor;
				
				final Dialog dialogColorPicker = new Dialog(MainActivity.this);
				dialogColorPicker.setTitle(getResources().getString(R.string.label_backcolor));
				dialogColorPicker.setContentView(R.layout.dialog_color_picker);
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
				picker.setOldCenterColor(bgColor);
				picker.setNewCenterColor(bgColor);
				
				//to turn of showing the old color
				picker.setShowOldCenterColor(true);
				
				
				dialogColorPicker.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						
						Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_BG_COLOR, picker.getColor());
						MainActivity.this.drawingView.setBackgroundColor(picker.getColor());
						buttonBackColor.setBackgroundColor(picker.getColor());
					}
				});
				
				dialogColorPicker.show();
			}
		});
		
		final EditText editTextStrokeSize = (EditText) dialog.findViewById(R.id.edittext_stroke_size);
		editTextStrokeSize.setText(size == 0 ? 12+"" : size+"");
		
		/*final NumberPicker numberPickerSize = (NumberPicker) dialog.findViewById(R.id.numberpicker_size);
		numberPickerSize.setMaxValue(50);
		numberPickerSize.setMinValue(1);
		numberPickerSize.setValue(size == 0 ? 12 : size);*/
		
		final EditText editTextEraserSize = (EditText) dialog.findViewById(R.id.edittext_eraser_size);
		editTextEraserSize.setText(eSize == 0 ? 12+"" : eSize+"");
		
		/*final NumberPicker numberPickerEraser = (NumberPicker) dialog.findViewById(R.id.numberpicker_eraser);
		numberPickerEraser.setMaxValue(50);
		numberPickerEraser.setMinValue(1);
		numberPickerEraser.setValue(eraserSize == 0 ? 12 : eraserSize);*/
		
		final Button buttonResetSettings = (Button) dialog.findViewById(R.id.button_default);
		buttonResetSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				MainActivity.this.mPaint.setColor(Color.BLACK);
				MainActivity.this.drawingView.setBackgroundColor(Color.WHITE);
				buttonForeColor.setBackgroundColor(Color.BLACK);
				buttonBackColor.setBackgroundColor(Color.WHITE);
				buttonShowCircle.setText("OFF");
				editTextStrokeSize.setText(getResources().getString(R.string.label_default_size));
				MainActivity.this.mPaint.setStrokeWidth(Integer.parseInt(getResources().getString(R.string.label_default_size)));
				editTextEraserSize.setText(getResources().getString(R.string.label_default_eraser_size));
				
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_SHOW_CIRCLE, 0);
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_FORE_COLOR, 0);
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_BG_COLOR, 0);
				/*Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_STROKE_SIZE, Integer.parseInt(getResources().getString(R.string.label_default_size)));
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_ERASER_SIZE, Integer.parseInt(getResources().getString(R.string.label_default_eraser_size)));*/
			}
		});
		
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				
				int strokeSize = size;
				int eraserSize = eSize;
				
				try {
					strokeSize = Integer.parseInt(editTextStrokeSize.getText().toString().trim().equals("") ? "12" : editTextStrokeSize.getText().toString().trim());
					eraserSize = Integer.parseInt(editTextEraserSize.getText().toString().trim().equals("") ? "12" : editTextEraserSize.getText().toString().trim());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
				if(strokeSize > 100 || strokeSize < 1) {
					Utils.showToast(MainActivity.this, "0 < " + getResources().getString(R.string.label_size) + " < 101");
					strokeSize = size;
				}
				
				if(eraserSize > 100 || eraserSize < 1) {
					Utils.showToast(MainActivity.this, "0 < " + getResources().getString(R.string.label_eraser) + " < 101");
					eraserSize = eSize;
				}
				
				drawingView.setShowCircle("ON".equals(buttonShowCircle.getText().toString()));
				if(eraserEnabled) {
					MainActivity.this.mPaint.setStrokeWidth(eraserSize);
				} else {
					MainActivity.this.mPaint.setStrokeWidth(strokeSize);
				}
				
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_SHOW_CIRCLE, "ON".equals(buttonShowCircle.getText().toString()) ? 1 : 0);
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_STROKE_SIZE, strokeSize);
				Utils.saveIntegerPreferences(MainActivity.this, Constants.KEY_ERASER_SIZE, eraserSize);
			}
		});
	}
	
	private void viewImages() {
		
		String folderPath = Environment.getExternalStorageDirectory().getPath() +"/sketchpen/";

		File folder = new File(folderPath);
		
		if(!folder.exists()) {
			Utils.showToast(MainActivity.this, getResources().getString(R.string.toast_message_file_not_found));
			return;
		}
		
		String[] allFiles = folder.list();
		
		if(allFiles.length > 0) {
			mediaScanFilePath = Environment.getExternalStorageDirectory().toString()+"/sketchpen/"+allFiles[allFiles.length-1];
		} else {
			Utils.showToast(MainActivity.this, getResources().getString(R.string.toast_message_file_not_found));
			return;
		}
        if(allFiles.length > 0) {
        	progress = ProgressDialog.show(this, 
        			getResources().getString(R.string.dialogue_scanning_media_title), 
        			getResources().getString(R.string.dialogue_scanning_media_body));
        	startScan();
        } else {
        	Toast.makeText(this, getResources().getString(R.string.toast_scanning_media_not_found), Toast.LENGTH_SHORT).show();
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
		
		// Start refreshing galary
		ContentValues values = new ContentValues();
	    values.put(MediaStore.Images.Media.DATA, imagePath);
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
	    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	    // End refreshing galary
	    
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
    	   Toast.makeText(this, getResources().getString(R.string.toast_save_image_error), Toast.LENGTH_SHORT).show();
    	   Utils.saveIntegerPreferences(this, Constants.KEY_IMAGE_COUNTER, newImageCount-1);
       }
       return imagePath;
	}
	
	private void saveImageAndShareWithFriend() {
		
		String imagePath = saveImage();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imagePath));
		intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_image_text));
		startActivityForResult(Intent.createChooser(intent , getResources().getString(R.string.chooser_share_title)),0);
	}
	
	private void insertImage() {

		int width = drawingView.getMeasuredWidth();
		int height = drawingView.getMeasuredHeight();
		
		Log.d(TAG, "width : " + width);
		Log.d(TAG, "height : " + height);
		
		// Code for crop image
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
        .setType("image/*")
        .putExtra("crop", "true")
        .putExtra("aspectX", width)
        .putExtra("aspectY", height)
        .putExtra("outputX", width)
        .putExtra("outputY", height)
        .putExtra("scale", true)
        .putExtra("scaleUpIfNeeded", true)
        .putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

		try {
			intent.putExtra("return-data", true);
			startActivityForResult(Intent.createChooser(intent,
					getResources().getString(R.string.action_insert_image)), SELECT_PICTURE);

		} catch (ActivityNotFoundException e) {
			
			// Code for select image
	        intent.removeExtra("crop");
	        intent.removeExtra("aspectX");
	        intent.removeExtra("aspectY");
	        intent.removeExtra("outputX");
	        intent.removeExtra("outputY");
	        intent.removeExtra("scale");
	        intent.removeExtra("scaleUpIfNeeded");
	        intent.removeExtra("outputFormat");
	        
	        intent.setAction(Intent.ACTION_GET_CONTENT);
	        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.action_insert_image)), SELECT_PICTURE);
		}
		
	}
	
	private void resetImage() {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getResources().getString(R.string.alert_reset_image_title));
		dialog.setMessage(getResources().getString(R.string.alert_reset_image_body));
		dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        	dialog.cancel();
	        	/*Intent intent = getIntent();
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            finish();
	            startActivityForResult(intent, 0);*/
	        	drawingView.clear();
	        	
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                	drawingView.setBackgroundDrawable(null);
                } else {
                	drawingView.setBackground(null);
                }

                int bgColor = Utils.getIntegerPreferences(MainActivity.this, Constants.KEY_BG_COLOR);
                drawingView.setBackgroundColor(bgColor);
	        }
	     });
		dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	dialog.cancel();
	        }
	     });
	    
		dialog.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
        if (resultCode == RESULT_OK) {
        	
        	if (requestCode == SELECT_PICTURE) {
    			Bundle extras2 = data.getExtras();
    			if (extras2 != null) {
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
	
	@Override
	public void onBackPressed() {
		
		new AlertDialog.Builder(this)
		.setTitle(getResources().getString(R.string.alert_close_app_title))
		.setMessage(getResources().getString(R.string.alert_close_app_body))
		.setPositiveButton(android.R.string.yes,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					MainActivity.super.onBackPressed();
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
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		if(adView != null) {
			adView.destroy();
		}
		
		if(interstitial != null) {
			interstitial.stopLoading();
		}
		
		Log.d(TAG, "onDestroy");
	}
}
