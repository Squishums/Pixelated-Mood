package com.squishums.android.pixelatedmood.system;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.squishums.android.pixelatedmood.component.ComponentPhysics;
import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.drop.Drop;
import com.squishums.android.pixelatedmood.drop.DropManager;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferences;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferencesManager;


/**
 * The system for touch-related drop movement.
 */
public class SystemPulsar extends PixelatedSystem {
	
	// Number of touch points on the screen to keep track up. Some screens
	// do not support multi touch, or limit the number of pointers. This is
	// only the *maximum* possible pointers to track.
	private static final int POINTERS_TO_TRACK = 5;
	
	private final float[][] mPointers = new float[POINTERS_TO_TRACK][2];
	private final List<float[]> mActivePointers =
			new ArrayList<float[]>(POINTERS_TO_TRACK);
	
	
	protected SystemPulsar() { }
	
	public void process(Canvas canvas) {
		float[] pointer = null;
		DropManager dropManager = Drop.getDropManager();
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		for (Drop drop : dropManager.getBoundDrops()) {
			ComponentPosition positionComponent;
			ComponentPhysics physicsComponent;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
				physicsComponent = drop.getComponent(ComponentPhysics.class);
			} catch (IllegalArgumentException e) {
				// Drop is not set up to be pushed
				continue;
			}
			
			// Avoiding For Each syntax here to prevent allocation of a new
			// iterator AND float[] for every drop every frame. It was a lot
			// of wasted memory, causing the GC to go crazy.
			int pointerCount = mActivePointers.size();
			for (int i = 0; i < pointerCount; i++) {
				pointer = mActivePointers.get(i);
				
				float deltaX = pointer[0] - positionComponent.x;
				float deltaY = pointer[1] - positionComponent.y;
				float distance = Math.max(
						FloatMath.sqrt(deltaX*deltaX + deltaY*deltaY),
						prefs.pulsarPrefs.minDistance);
				
				float force = getForce(
						prefs.pulsarPrefs.force,
						prefs.pulsarPrefs.falloffExponent,
						distance);
				
				physicsComponent.veloX += force * deltaX / distance;
				physicsComponent.veloY += force * deltaY / distance;
			}
			
			// Push the drop
			positionComponent.x += physicsComponent.veloX;
			positionComponent.y += physicsComponent.veloY;
			// Dampen the velocity
			physicsComponent.veloX *=
					prefs.physicsPrefs.velocityDampeningFactor;
			physicsComponent.veloY *=
					prefs.physicsPrefs.velocityDampeningFactor;
		}
	}
	
	/**
	 * Return the force that should be applied to a drop based on the given
	 * information.
	 * 
	 * @param force - the force specified in the drop set's preferences.
	 * @param falloff - the falloff specified in the drop set's preferences.
	 * @param distance - the absolute distance between the pointer and the
	 * 		drop.
	 * @return the absolute force to apply to the drop.
	 */
	private float getForce(float force, float falloff, float distance) {
		return force / (float) Math.pow(distance, falloff);
	}
	
	/**
	 * This should be called for ever motion event.
	 * 
	 * @param event
	 */
	public void onTouch(MotionEvent event) {
		// Android has a *really* weird pseudo-C pointer management system.
		// Please don't ask me to explain it; just know that the pointer Id
		// uniquely identifies each pointer and is stable.
		final int pointerIndex = event.getActionIndex();
		final int pointerId = event.getPointerId(pointerIndex);
		if (pointerId >= POINTERS_TO_TRACK) {
			return;
		}
		
		switch(event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			// A new pointer has been put down.
			mPointers[pointerId][0] = event.getX(pointerIndex);
			mPointers[pointerId][1] = event.getY(pointerIndex);
			mActivePointers.add(mPointers[pointerId]);
			break;
		}
		
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP: {
			// A pointer has been lifted.
			mActivePointers.remove(mPointers[pointerId]);
			if (event.getPointerCount() == 0) {
				// See if this fixes the issues where pointers would remin even
				// after being lifted. The lift-up message for some pointers
				// might not be getting through.
				//
				// This doesn't guarentee that the problem never occurs, but it
				// allows the user to fix the problem by simply touching the
				// screen again.
				mActivePointers.clear();
			}
			break;
		}
		
		case MotionEvent.ACTION_MOVE: {
			// A pointer has moved.
			int pointerCount = event.getPointerCount();
			for (int i = 0; i < pointerCount; i++) {
				int id = event.getPointerId(i);
				
				if (id < POINTERS_TO_TRACK) {
					mPointers[id][0] = event.getX(id);
					mPointers[id][1] = event.getY(id);
				}
			}
			break;
		}
		}
	}
}
