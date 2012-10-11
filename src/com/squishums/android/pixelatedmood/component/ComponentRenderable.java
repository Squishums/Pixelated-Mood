package com.squishums.android.pixelatedmood.component;

import android.graphics.Bitmap;
import android.graphics.Paint;

public class ComponentRenderable extends PixelatedComponent{

	// The bitmap for this drop.
	public Bitmap bitmap;
	// The filter to be used when drawing the drop.
	public Paint filter;
	
	public ComponentRenderable() { }
}
