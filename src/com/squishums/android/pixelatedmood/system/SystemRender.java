package com.squishums.android.pixelatedmood.system;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.component.ComponentRenderable;
import com.squishums.android.pixelatedmood.drop.Drop;


/**
 * Rendering system for the drops. All drop drawing is done here.
 */
public class SystemRender extends PixelatedSystem {

	protected SystemRender() { }
	
	public void process(Canvas canvas) {
		for (Drop drop : Drop.getDropManager().getBoundDrops()) {
			ComponentRenderable renderComponent;
			ComponentPosition positionComponent;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
				renderComponent = drop.getComponent(ComponentRenderable.class);
			} catch (IllegalArgumentException e) {
				// Drop is not renderable
				continue;
			}
			
			Paint filter = renderComponent.filter;
			Bitmap bitmap = renderComponent.bitmap;
			float posX = positionComponent.x;
			float posY = positionComponent.y;
			
			canvas.drawBitmap(bitmap, posX, posY, filter);
		}
	}
}
