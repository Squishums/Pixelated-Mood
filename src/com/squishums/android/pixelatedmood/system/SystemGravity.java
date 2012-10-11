package com.squishums.android.pixelatedmood.system;

import java.util.Arrays;

import android.graphics.Canvas;
import android.util.FloatMath;
import android.util.Log;

import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.drop.Drop;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferences;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferencesManager;


/**
 * PixelatedSystem used to implement the affect of gravity.
 */
public class SystemGravity extends PixelatedSystem {

	private static final String LOG_TAG = SystemGravity.class.getName();
	// Minimum change in the X, Y or Z value of the gravity vector required
	// to update the internal force values.
	private static final int GRAVITY_TOLERANCE = 1;
	
	private float[] mGravityVector = new float[] {0, 0, 9.8f};
	// Angle of gravity along the XY plane.
	private float mGravityAngleXY = 0;
	// Force of gravity along the X and Y axes.
	private float mGravityFactorX = 0;
	private float mGravityFactorY = 0;
	// Magnitude of gravity along the XY plane (projected onto XY)
	private float mGravityMagnitudeXY = 0;
	// Absolute value of gravity. Is affected by other accelorations that the
	// phone is going through.
	private float mGravityMagnitude = 0;
	
	
	protected SystemGravity() { }
	
	public void process(Canvas canvas) {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		for (Drop drop : Drop.getDropManager().getBoundDrops()) {
			ComponentPosition positionComponent = null;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Drop is lacking a position component");
				continue;
			}
			
			positionComponent.x -= prefs.gravityPrefs.force
					* mGravityFactorX;
			positionComponent.y -= prefs.gravityPrefs.force
					* mGravityFactorY;
		}
	}
	
	/**
	 * Call this when the phone's 3D orientation changes to update the gravity.
	 * The Z-coordinate may or may not be used, depending on the current drop
	 * settings.
	 * 
	 * @param vector - the 3D vector reported by the phone's gravity sensor,
	 * 		in the form of [x, y, z].
	 */
	protected void onOrientationChange(float[] vector) {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		float deltaX = Math.abs(mGravityVector[0] - vector[0]);
		float deltaY = Math.abs(mGravityVector[1] - vector[1]);
		float deltaZ = Math.abs(mGravityVector[2] - vector[2]);
		
		if (deltaX > GRAVITY_TOLERANCE
				|| deltaY > GRAVITY_TOLERANCE
				|| deltaZ > GRAVITY_TOLERANCE) {
			// Only update the gravity if the change is significant to avoid
			// the high cost of floating point mathematics.
			
			mGravityVector = Arrays.copyOf(vector, 3);

			mGravityMagnitudeXY = FloatMath.sqrt(
					vector[0]*vector[0] + vector[1]*vector[1]);
			mGravityAngleXY = (float) Math.acos(
					vector[0] / mGravityMagnitudeXY);
			mGravityAngleXY *= Math.signum(vector[1]);
			
			if (prefs.gravityPrefs.useZ) {
				// Recalculate the strength of the gravity to include the Z axis
				mGravityMagnitude = FloatMath.sqrt(
						vector[0]*vector[0]
						+ vector[1]*vector[1]
						+ vector[2]*vector[2]);
			}
			mGravityFactorX = vector[0] / mGravityMagnitude;
			mGravityFactorY = -vector[1] / mGravityMagnitude;
		}
	}
	
	/**
	 * Returns the angle along the XY plane of the gravity currently being used
	 * by the gravity system. If the gravity system isn't being used by the
	 * current drop set, this value is undefined.
	 * 
	 * @return the angle of gravity along the XY plane, or undefined if the
	 * 		system isn't in use.
	 */
	protected float getGravityAngleXY() {
		return mGravityAngleXY;
	}
}
