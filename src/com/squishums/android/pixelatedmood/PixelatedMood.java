package com.squishums.android.pixelatedmood;

/*
 * Pixelated Mood is an animated Live Wallpaper designed for the Android 2.3
 * operating system. The wallpaper mainly features falling "drops" that are
 * affected through various input channels. There are multiple different "sets",
 * a set of rules that define how the drops are drawn, react to different
 * stimulie, etc, defined in the res/raw directory and parsed in
 * DropPresetsXmlParser.java.
 * 
 * The drop system uses a Component Entity System (CES) at its base. An entity 
 * exists only as a unique identifying number and a list of "components", which
 * are packages of data, but no logic (see the .component package). The actual
 * logic is done through "systems" (see the .system package). The SystemManager
 * controls the activation of the different systems, while the DropManager
 * provides methods to find drops and their components.
 * 
 * Rendering is fairly simple, and is mostly blitting bitmaps. Preformance
 * would increase substantially were the rendering system to use OpenGL, but
 * such support is not natively available for the target Android version, so
 * it has been omitted in this project for simplicity.
 */

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.squishums.android.pixelatedmood.drop.Drop;
import com.squishums.android.pixelatedmood.drop.DropManager;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferences;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferencesManager;
import com.squishums.android.pixelatedmood.system.SystemManager;

public class PixelatedMood extends WallpaperService {
	
	/** Delay between frame draws (ms) */
	public static final int FRAME_DELAY = 33;
	
	/** Thread handler for the drawing thread */
	private Handler mHandler = new Handler();
	
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public Engine onCreateEngine() {
		return new DropfallEngine();
	}

	
	public class DropfallEngine extends Engine
				implements OnSharedPreferenceChangeListener {
		
		private final String LOG_TAG = DropfallEngine.class.getName();
		
		private SystemManager mSystemManager;
		// DEBUG */ private FramerateCounter mFramerateCounter;
		
		// Whether preferences have changed between visibility state changes
		private boolean mPreferencesChanged = false;
		
		private int mScreenWidth = 1;
		private int mScreenHeight = 1;
		
		// Timestamp of last render
		private long mLastRender;
		
		private int mRenderCounter = 0;
		private final Runnable mDropDrawer = new Runnable() {
			
			@Override
			public void run() {
				// The delay between frames is broken into 2 steps to avoid
				// delays between the rendering thread waking and beginning
				// execution. Waiting the entire de
				long deltaTime = SystemClock.elapsedRealtime() - mLastRender;
				if (mRenderCounter % 3 == 0) {
					try {
						Thread.sleep(FRAME_DELAY / 3);
					} catch (InterruptedException e) {
						// Doesn't matter, but log it anyway.
						Log.e(LOG_TAG, "Framerate limiter interrupted");
					}
				} else if (mRenderCounter % 3 == 1
						&& FRAME_DELAY > deltaTime) {
					try {
						Thread.sleep(FRAME_DELAY - deltaTime);
					} catch (InterruptedException e) {
						// Doesn't matter, but log it anyway.
						Log.e(LOG_TAG, "Framerate limiter interrupted");
					}
				} else if (mRenderCounter % 3 == 2) {
					mLastRender = SystemClock.elapsedRealtime();
					updateScreen();
				}
				
				mRenderCounter++;
				mHandler.post(mDropDrawer);
			}
		};
		
		// Bitmap and Canvas used when the "trails" options is on. 
		private Bitmap mTrailBitmap;
		private Canvas mTrailCanvas;
		
		
		DropfallEngine() {
			mLastRender = SystemClock.elapsedRealtime();
			
			Drop.setDropManager(new DropManager());
			mSystemManager = new SystemManager();
			// DEBUG */ mFramerateCounter = new FramerateCounter();
			
			// Preferences
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(PixelatedMood.this);
			sharedPrefs.registerOnSharedPreferenceChangeListener(this);
			PixelatedPreferencesManager.loadPresets(PixelatedMood.this);

			onPreferencesUpdated();
			
			this.setTouchEventsEnabled(true);
		}
		
		@Override
		public void onCreate(SurfaceHolder holder) {
			super.onCreate(holder);
			// Ensure the proper color depth for bitmap blitting
			holder.setFormat(PixelFormat.RGBA_8888);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible) {
				if (mPreferencesChanged) {
					onPreferencesUpdated();
					mPreferencesChanged = false;
				}
				
				// DEBUG */ mFramerateCounter.resume();
				
				mHandler.post(mDropDrawer);
			} else {
				// DEBUG */ mFramerateCounter.pause();
				
				mHandler.removeCallbacks(mDropDrawer);
			}
			
			mSystemManager.onVisibilityChanged(PixelatedMood.this, visible);
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			
			PixelatedPreferences prefs =
					PixelatedPreferencesManager.getCurrentPreferences();
			
			mScreenWidth = width;
			mScreenHeight = height;
			mSystemManager.setScreenDimensions(width, height);
			
			if (prefs.renderPrefs.trails) {
				createTrailCanvas();
			}
		}
		
		@Override
		public void onTouchEvent(MotionEvent event) {
			mSystemManager.onTouch(event);
		}
		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences prefs, String key) {
			mPreferencesChanged = true;
		}
		
		/**
		 * Update the global preferences to match the selected set
		 */
		public void onPreferencesUpdated() {
			PixelatedPreferences prefs =
					PixelatedPreferencesManager.getCurrentPreferences();
			
			mSystemManager.onPreferencesUpdated();
			Drop.getDropManager().setDropCount(
					PixelatedPreferencesManager.getCurrentPreferences()
						.dropPrefs.count);
			
			if (prefs.renderPrefs.trails) {
				createTrailCanvas();
			} else {
				mTrailBitmap = null;
			}
		}
		
		/*
		 * Create or update the trails canvas.
		 */
		private void createTrailCanvas() {
			PixelatedPreferences prefs =
					PixelatedPreferencesManager.getCurrentPreferences();
			if (mTrailBitmap != null) {
				mTrailBitmap.recycle();
			}
			
			mTrailBitmap = Bitmap.createBitmap(
					mScreenWidth, mScreenHeight,
					Bitmap.Config.ARGB_8888);
			mTrailCanvas = new Canvas(mTrailBitmap);
			
			mTrailCanvas.drawPaint(prefs.backgroundColor);
		}
		
		/**
		 * Compute logic and draw the current drop state to the screen. Nothing
		 * happens if the thread is unable to acquire the screen canvas' lock.
		 */
		private void updateScreen() {
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				PixelatedPreferences prefs =
						PixelatedPreferencesManager.getCurrentPreferences();
				if (canvas != null) {
					if (prefs.renderPrefs.trails) {
						mSystemManager.process(mTrailCanvas);
						canvas.drawBitmap(mTrailBitmap, 0, 0, null);
					} else {
						canvas.drawPaint(prefs.backgroundColor);
						mSystemManager.process(canvas);
					}
					
					// DEBUG */ mFramerateCounter.frame();
					// DEBUG */ mFramerateCounter.printFPS(canvas, 10, 50);
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}