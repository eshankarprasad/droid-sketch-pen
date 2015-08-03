package com.devdroid.sketchpen.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private boolean showCircle;
	private Paint canvasPaint;
    
    public DrawingView(Context c) {
        super(c);
        context=c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f); 
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);

    	mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    	mCanvas = new Canvas(mBitmap);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
    	canvas.drawPath( mPath,  canvasPaint);
    	if(showCircle) {
    		canvas.drawPath(circlePath, circlePaint);
    	}
    }

    public void setCanvasPaint(Paint mPaint) {
		this.canvasPaint = mPaint;
	}

	private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
	private static final String TAG = "DrawingView";
    
    private void touch_start(float x, float y) {
    	mPath.reset();
    	mPath.moveTo(x, y);
    	mX = x;
    	mY = y;
    }
    
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
             mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  canvasPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

	public boolean isShowCircle() {
		return showCircle;
	}

	public void setShowCircle(boolean showCircle) {
		this.showCircle = showCircle;
	} 
	
	public void clear() {
		mCanvas.drawColor(0, Mode.CLEAR);
		this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
	}
	
	public void setBackgroundBitmap(Bitmap bitmap) {
		
		Point size = Utils.getScreenSize((Activity)context);
		int width = size.x;
		int height = size.y;
		//Log.d(TAG, "Resolution: " + bitmap.getWidth() + " / " + bitmap.getHeight());
		//Bitmap.createScaledBitmap(bitmap, width*4, height*4, true)
		
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
		}
		
		mCanvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, width, height), canvasPaint);
		/*Drawable drawable = new BitmapDrawable(bitmap)
		super.setb;*/
	}
	
}