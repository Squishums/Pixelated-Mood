package com.squishums.android.pixelatedmood.system;

import android.graphics.Canvas;

import com.squishums.android.pixelatedmood.component.ComponentPhysics;
import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.drop.Drop;


/**
 * The system for applying a uniform force to all drops.
 */
public class SystemWind extends PixelatedSystem {
		
	protected SystemWind() { }
	
	public void process(Canvas canvas) {
		for (Drop drop : Drop.getDropManager().getBoundDrops()) {
			ComponentPhysics physicsComponent;
			ComponentPosition positionComponent;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
				physicsComponent = drop.getComponent(ComponentPhysics.class);
			} catch (IllegalArgumentException e) {
				// Drop is not set up to fall.
				continue;
			}
			
			positionComponent.x += physicsComponent.windFactorX;
			positionComponent.y += physicsComponent.windFactorY;
		}
	}
}
