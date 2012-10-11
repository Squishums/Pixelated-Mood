package com.squishums.android.pixelatedmood.system;

import android.graphics.Canvas;


/**
 * Abstract base class from which each system in the CES is derived. Derived
 * classes should implement process(), which is called every frame, to do their
 * work.
 */
public abstract class PixelatedSystem {
	
	/**
	 * Implement this to do work.
	 * 
	 * @param canvas - the canvas to draw to for this fram. This may not be
	 * 		the canvas for the actual screen, depending on the rendering method. 
	 */
	abstract protected void process(Canvas canvas);
}
