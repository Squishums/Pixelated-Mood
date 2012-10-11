package com.squishums.android.pixelatedmood.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;

import com.squishums.android.pixelatedmood.PixelatedMood;

/**
 * The FramerateCounter class provides a simple way of drawing realtime FPS data
 * to a canvas object. The counter starts inactive, and must be started by
 * calling resume(). Each frame is counted by calling the frame() method.
 * 
 * Note that there is an approximately two second delay between first starting
 * the framerate counter and the tracking of frames. frame() must be called
 * during this time, but do not expect any data for a couple of seconds. This
 * does not apply to subsequent resumes after the first.
 */
public class FramerateCounter {
	
	// Number of frames to skip before starting to track times. The initial
	// frames will be unreliable due to the initialization not having completed.
	private static final int FRAMES_TO_SKIP = 120;
	// Number of frames to base the FPS data on.
	private static final int FRAMES_TO_TRACK = 5;
	
	// Position data for the text.
	private static final int LINE_SPACING = 5;
	private static final int PADDING_HORIZONTAL = 10;
	private static final int PADDING_VERTICAL = 5;
	
	// Converting the framerate numbers to strings causes the GC to go
	// absolutely insane, so limit how often the strings are updated. This
	// number can be lowered when up-to-date FPS data is needed, but the data
	// becomes less accurate as the updates get more frequent.
	private static final int STRING_UPDATE_DELAY =
			2000 / PixelatedMood.FRAME_DELAY;

	private boolean mActive = false;
	// Time of the first frame (for average fps calculation).
	private long mFirstFrameTime;
	// Number of frames
	private int mFrameCount = 0;
	private long[] mRecentFrameTimes = new long[FRAMES_TO_TRACK];
	private float mAverageFramerate = 0;
	private float mCurrentFramerate = 0;
	private float mLowestFramerate = -1;
	
	// Class-wide strings to prevent having to create new strings every frame,
	// which causes too many allocations and skews the FPS data.
	private String mAverageFramerateString = "";
	private String mCurrentFramerateString = "";
	private String mLowestFramerateString = "";
	
	// Font drawing data
	private int mFontHeight;
	private int mFontWidth;
	private Paint mFontPaint;
	private Paint mBackgroundPaint;
	
	/**
	 * Creates a basic framerate counter.
	 */
	public FramerateCounter() {
		mFontPaint = new Paint();
		mFontPaint.setColor(0xFFE0E0E0);
		mFontPaint.setTextSize(20);
		mFontPaint.setAntiAlias(true); // Turn off for performance.
		calculateFontSize();
		
		mBackgroundPaint = new Paint();
		mBackgroundPaint.setColor(0x88FF0000);
		
		mFirstFrameTime = SystemClock.elapsedRealtime();
	}
	
	/**
	 * Call this function once per frame for every frame while the frame counter
	 * is active. Calls done while the framerate counter is paused will have no effect.
	 */
	public void frame() {
		if (!mActive) { return; }
		
		int currentFrame = mFrameCount % FRAMES_TO_TRACK;
		mRecentFrameTimes[currentFrame] = SystemClock.elapsedRealtime();
		
		if (mFrameCount > FRAMES_TO_SKIP) {
			// Don't count the first few frames
			mAverageFramerate = (1000 * mFrameCount)
					/ (SystemClock.elapsedRealtime() - mFirstFrameTime);
			
			mCurrentFramerate = 0;
			for (int i = 0; i < FRAMES_TO_TRACK - 1; i++) {
				int frame1 = (currentFrame - i) % FRAMES_TO_TRACK;
				frame1 = (frame1 < 0) ? frame1 + FRAMES_TO_TRACK : frame1;
				int frame2 = (currentFrame - i - 1) % FRAMES_TO_TRACK;
				frame2 = (frame2 < 0) ? frame2 + FRAMES_TO_TRACK : frame2;
				mCurrentFramerate +=
						mRecentFrameTimes[frame1] - mRecentFrameTimes[frame2]; 
			}
			mCurrentFramerate = 1000 * (FRAMES_TO_TRACK - 1) / mCurrentFramerate;
			
			if (mLowestFramerate < 0
					|| mCurrentFramerate < mLowestFramerate
					&& mCurrentFramerate != 0) {
				mLowestFramerate = mCurrentFramerate;
			}
		}
		mFrameCount++;
	}
	
	/**
	 * Print the FPS to the canvas. This should be called after all other draws
	 * have completed to ensure that the FPS counter is on top.
	 * 
	 * @param canvas - the canvas to draw to.
	 * @param x - the coordinate of the left side of the FPS counter.
	 * @param y - the coordinate of the top side of the FPS counter.
	 */
	public void printFPS(Canvas canvas, int x, int y) {
		if (mFrameCount < FRAMES_TO_SKIP) {
			return;
		}
		
		canvas.drawRect(
				x - PADDING_HORIZONTAL,
				y - PADDING_VERTICAL,
				x + mFontWidth + PADDING_HORIZONTAL,
				y + mFontHeight * 4 + PADDING_VERTICAL,
				mBackgroundPaint);
		
		if (mFrameCount % STRING_UPDATE_DELAY == 0) {
			mAverageFramerateString = "Current: " + ((int) mCurrentFramerate);
			mCurrentFramerateString = "Average: " + ((int) mAverageFramerate);
			mLowestFramerateString = "Lowest:  " + ((int) mLowestFramerate);
		}
		
		int offset = mFontHeight;
		canvas.drawText("--------FPS--------", x, y + offset, mFontPaint);
		offset += mFontHeight;
		canvas.drawText(mAverageFramerateString, x, y + offset, mFontPaint);
		offset += mFontHeight;
		canvas.drawText(mCurrentFramerateString, x, y + offset, mFontPaint);
		offset += mFontHeight;
		canvas.drawText(mLowestFramerateString, x, y + offset, mFontPaint);
	}
	
	/**
	 * Calculate the font width and height required.
	 */
	private void calculateFontSize() {
		Rect bounds = new Rect();
		mFontPaint.getTextBounds("--------FPS--------", 0, 19, bounds);
		mFontWidth = bounds.width();
		mFontHeight = bounds.height() + LINE_SPACING;
	}
	
	/**
	 * Pause the framerate counter. frame() calls will be ignored until the
	 * counter is resumed.
	 */
	public void pause() {
		mActive = false;
		mFirstFrameTime = SystemClock.elapsedRealtime();
		mFrameCount = 0;
	}
	
	/**
	 * Resume the framerate counter. frame() calls will be counted until the
	 * counter is paused.
	 */
	public void resume() {
		mActive = true;
		mFirstFrameTime = SystemClock.elapsedRealtime();
		mFrameCount = 0;
	}
}
