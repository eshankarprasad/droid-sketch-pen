package com.devdroid.sketchpen;

import info.androidhive.slidingmenu.adapter.NavDrawerListAdapter;
import info.androidhive.slidingmenu.model.NavDrawerItem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.devdroid.sketchpen.util.IabHelper;
import com.devdroid.sketchpen.util.IabResult;
import com.devdroid.sketchpen.util.Inventory;
import com.devdroid.sketchpen.util.Purchase;
import com.devdroid.sketchpen.utility.Constants;
import com.devdroid.sketchpen.utility.DrawingView;
import com.devdroid.sketchpen.utility.Utils;
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
public class SketchPenActivity extends Activity implements MediaScannerConnectionClient {
	
	private static final String TAG = "SketchPen";
	
	// Test ITEM_SKU
	//private static final String ITEM_SKU = "android.test.purchased";
	//private static final String ITEM_SKU = "android.test.cancelled";
	//private static final String ITEM_SKU = "android.test.refunded";
	//private static final String ITEM_SKU = "android.test.item_unavailable";
	
	// Production
	private static final String ITEM_SKU = "com.devdroid.sketchpen.adfree";
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
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, getString(R.string.eraser_inactive)));
		
		// Erase all
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
		
		// Buy Ad free
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
		
		// Rating and review
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
		
		// Like us
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1)));

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
	                		   Utils.showToast(SketchPenActivity.this, getString(R.string.toast_save_image_success));
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
	                	   if(getString(R.string.eraser_active).equals(navDrawerItems.get(position).getCount())) {
	                		   navDrawerItems.get(position).setCount(getString(R.string.eraser_inactive));
	                		   int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
	                		   int width = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE);
	                		   int showCircle = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE);
	                		   foreColor = foreColor == 0 ? Color.BLACK : foreColor;
	                		   width = width == 0 ? 12 : width;
	                		   mPaint.setStrokeWidth(width);
	                		   mPaint.setColor(foreColor);
	                		   drawingView.setShowCircle(showCircle == 1);
	                		   mPaint.setXfermode(null);
	                		   eraserEnabled = false;
	                		   
	                	   } else {
	                		   navDrawerItems.get(position).setCount(getString(R.string.eraser_active));
	                		   int eraserSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);
	                		   mPaint.setStrokeWidth(eraserSize);
	                		   drawingView.setShowCircle(true);
	                	       mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	                	       mPaint.setAntiAlias(true);
	                	       eraserEnabled = true;
	                	       Utils.showToast(SketchPenActivity.this, getString(R.string.label_message_eraser));
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
			// Buy Ad free
			runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	            	
	            	if(Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED) == 0) {
	            		buyAdFree();
	            	} else {
	            		Utils.showToast(SketchPenActivity.this, "Already bought");
	            	}
	            }
	        });
			break;
		case 8:
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
			
		case 9:
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
						rateYourApp();
					}
				});
			}
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
	    
	    FrameLayout container = (FrameLayout) findViewById(R.id.frame_container);
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

	private void rateYourApp() {
		
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
	
	private void loadAd(final Long showAd) {

		//adView = new AdView(SketchPenActivity.this, com.google.ads.AdSize.BANNER, "a1530ed9c34caf8");
        adView = new AdView(SketchPenActivity.this, com.google.ads.AdSize.BANNER, "ca-app-pub-1782443214800184/3456231350");
	    // Create the interstitial.
	    //interstitial = new InterstitialAd(SketchPenActivity.this, "a1530ed9c34caf8");
        interstitial = new InterstitialAd(SketchPenActivity.this, "ca-app-pub-1782443214800184/3456231350");
	    
		if (Utils.hasConnection(SketchPenActivity.this)) {
			// Initiate a generic request to load it with an ad
			AdRequest adRequest = new AdRequest();
			adView.loadAd(adRequest);

		    // Begin loading your interstitial.
		    interstitial.loadAd(adRequest);
			adView.setAdListener(new AdListener() {

				public void onReceiveAd(Ad arg0) {
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

				}

				public void onDismissScreen(Ad arg0) {

				}
			});
		}
		
		FrameLayout  layout = (FrameLayout) drawingView.getParent();		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
		
		final Dialog dialog = new Dialog(SketchPenActivity.this);
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(getString(R.string.action_settings));
		dialog.setContentView(R.layout.dialog_setting);
		dialog.show();
		
		int bgColor = Utils.getIntegerPreferences(this, Constants.KEY_BG_COLOR);
		int foreColor = Utils.getIntegerPreferences(this, Constants.KEY_FORE_COLOR);
		final int size = Utils.getIntegerPreferences(this, Constants.KEY_STROKE_SIZE);
		final int eSize = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE);
		int showCircle = Utils.getIntegerPreferences(this, Constants.KEY_SHOW_CIRCLE);
		
		final ToggleButton buttonShowCircle = (ToggleButton) dialog.findViewById(R.id.button_show_circle);
		buttonShowCircle.setChecked(showCircle == 1);
		
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
				
				int foreColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR);
				foreColor = foreColor == 0 ? foreColor = Color.BLACK : foreColor;
				
				final Dialog dialogColorPicker = new Dialog(SketchPenActivity.this);
				dialogColorPicker.setTitle(getString(R.string.label_forecolor));
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
						
						Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR, picker.getColor());
						SketchPenActivity.this.mPaint.setColor(picker.getColor());
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
				
				int bgColor = Utils.getIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR);
				bgColor = bgColor == 0 ? bgColor = Color.WHITE : bgColor;
				
				final Dialog dialogColorPicker = new Dialog(SketchPenActivity.this);
				dialogColorPicker.setTitle(getString(R.string.label_backcolor));
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
						
						Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, picker.getColor());
						SketchPenActivity.this.drawingView.setBackgroundColor(picker.getColor());
						buttonBackColor.setBackgroundColor(picker.getColor());
					}
				});
				
				dialogColorPicker.show();
			}
		});
		
		final TextView textViewStrokeSize = (TextView) dialog.findViewById(R.id.label_size);
		textViewStrokeSize.setText(getString(R.string.label_size) + ": " + (size == 0 ? 12 : size));
		
		final SeekBar seekBarStrokeSize = (SeekBar) dialog.findViewById(R.id.seekbar_stroke_size);
		seekBarStrokeSize.setProgress(size == 0 ? 12 : size);
		seekBarStrokeSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textViewStrokeSize.setText(getString(R.string.label_size) + ": " + (progress == 0 ? 1 : progress));
				if(progress == 0) {
					seekBar.setProgress(1);
				}
			}
		});
		
		final TextView textViewEraserSize = (TextView) dialog.findViewById(R.id.label_eraser);
		textViewEraserSize.setText(getString(R.string.label_eraser) + ": " + (eSize == 0 ? 12 : eSize));
		
		final SeekBar seekBarEraserSize = (SeekBar) dialog.findViewById(R.id.seekbar_eraser_size);
		seekBarEraserSize.setProgress(eSize == 0 ? 12 : eSize);
		seekBarEraserSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				textViewEraserSize.setText(getString(R.string.label_eraser) + ": " + (progress == 0 ? 1 : progress));
				if(progress == 0) {
					seekBar.setProgress(1);
				}
			}
		});
		
		final Button buttonResetSettings = (Button) dialog.findViewById(R.id.button_default);
		buttonResetSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				SketchPenActivity.this.mPaint.setColor(Color.BLACK);
				SketchPenActivity.this.drawingView.setBackgroundColor(Color.WHITE);
				buttonForeColor.setBackgroundColor(Color.BLACK);
				buttonBackColor.setBackgroundColor(Color.WHITE);
				buttonShowCircle.setChecked(false);
				seekBarStrokeSize.setProgress(Integer.parseInt(getString(R.string.label_default_size)));
				SketchPenActivity.this.mPaint.setStrokeWidth(Integer.parseInt(getString(R.string.label_default_size)));
				seekBarEraserSize.setProgress(Integer.parseInt(getString(R.string.label_default_eraser_size)));
				
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE, 0);
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_FORE_COLOR, 0);
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_BG_COLOR, 0);
			}
		});
		
		try {
			TextView textViewAppVersion = (TextView) dialog.findViewById(R.id.label_app_version);
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			textViewAppVersion.setText("V" + pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				
				int strokeSize = size;
				int eraserSize = eSize;
				
				strokeSize = seekBarStrokeSize.getProgress();
				eraserSize = seekBarEraserSize.getProgress();
				
				drawingView.setShowCircle(buttonShowCircle.isChecked());
				if(eraserEnabled) {
					SketchPenActivity.this.mPaint.setStrokeWidth(eraserSize);
				} else {
					SketchPenActivity.this.mPaint.setStrokeWidth(strokeSize);
				}
				
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_SHOW_CIRCLE, buttonShowCircle.isChecked() ? 1 : 0);
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_STROKE_SIZE, strokeSize);
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_ERASER_SIZE, eraserSize);
			
			}
		});
	}
	
	private void viewImages() {
		
		String folderPath = Environment.getExternalStorageDirectory().getPath() +"/sketchpen/";

		File folder = new File(folderPath);
		
		if(!folder.exists()) {
			Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found));
			return;
		}
		
		String[] allFiles = folder.list();
		
		if(allFiles.length > 0) {
			mediaScanFilePath = Environment.getExternalStorageDirectory().toString()+"/sketchpen/"+allFiles[allFiles.length-1];
		} else {
			Utils.showToast(SketchPenActivity.this, getString(R.string.toast_message_file_not_found));
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
			Utils.showToast(SketchPenActivity.this, getString(R.string.msg_general_error));
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
    				Utils.showToast(SketchPenActivity.this, getString(R.string.msg_external_storage_error));
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
					Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_canceled));
					
				} else if(result.getResponse() == -1008) {
					Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_refunded));
				}
				
				return;
			} else if (purchase.getSku().equals(ITEM_SKU)) {
				consumeItem();
			}
		}
	};
	
	@Override
	public void onMediaScannerConnected() {
		conn.scanFile(mediaScanFilePath, Constants.FILE_TYPE);
	}

	public void consumeItem() {
		mHelper.queryInventoryAsync(mReceivedInventoryListener);
	}
		
	IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {

			if (result.isFailure()) {
				Utils.showToast(SketchPenActivity.this, result.getMessage());
			} else {
				
				mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
						mConsumeFinishedListener);
			}
		}
	};

	IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			
			if (result.isSuccess()) {
				
				Utils.showToast(SketchPenActivity.this, getString(R.string.msg_payment_done));
				Utils.saveIntegerPreferences(SketchPenActivity.this, Constants.KEY_ITEM_PURCHASED, 1);
				finish();
				startActivity(getIntent());
			} else {
				Utils.showToast(SketchPenActivity.this, result.getMessage());
			}
		}
	};
	
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
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		if(adView != null) {
			adView.destroy();
		}
		
		if(interstitial != null) {
			interstitial.stopLoading();
		}
		
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
		
		Log.d(TAG, "onDestroy");
	}
	
	private void buyAdFree() {
		
		if(flagAdFree) {
			return;
		}
		
		flagAdFree = true;
		mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,   
  			   mPurchaseFinishedListener, "mypurchasetoken");
	}
}
