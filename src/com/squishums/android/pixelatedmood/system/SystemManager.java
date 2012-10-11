package com.squishums.android.pixelatedmood.system;

/*
 * Realistically, systems should be added in programmatically instead of using
 * boolean switches to determine on/off states. Since each system must derive
 * from PixelatedSystem, generics would allow for a single entry point for adding
 * systems. The logic for whether to run each system could be implemented into
 * the system itself as a 
 */

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;

import com.squishums.android.pixelatedmood.preferences.PixelatedPreferences;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferencesManager;


/**
 * Manager for the various systems in the CES. Systems should not be
 * interacted with directly, but rather through this manager.
 */
public class SystemManager {

	private SystemSensorListener mSensorListener = new SystemSensorListener();
	
	private SystemWind mWindSystem;
	private SystemGravity mGravitySystem;
	private SystemLifecycle mLifecycleSystem;
	private SystemPulsar mPulsarSystem;
	private SystemRender mRenderSystem;
	
	private boolean mUseWind;
	private boolean mUsePulsar;
	private boolean mUseGravity;
	
	public SystemManager() {
		mWindSystem = new SystemWind();
		mGravitySystem = new SystemGravity();
		mPulsarSystem = new SystemPulsar();
		mLifecycleSystem = new SystemLifecycle(mGravitySystem);
		mRenderSystem = new SystemRender();
	}
	
	/**
	 * Process the logic for each system in the CES.
	 * 
	 * @param canvas - the canvas to do any draw to.
	 */
	public void process(Canvas canvas) {
		if (mUseWind) {
			mWindSystem.process(canvas);
		}
		if (mUsePulsar) {
			mPulsarSystem.process(canvas);
		}
		if (mUseGravity) {
			mGravitySystem.process(canvas);
		}
		
		mLifecycleSystem.process(canvas);
		mRenderSystem.process(canvas);
	}
	
	/**
	 * Update the screen dimensions used by the systems. This should be called
	 * whenever the phone's orientation or resolution changes.
	 * 
	 * @param width
	 * @param height
	 */
	public void setScreenDimensions(int width, int height) {
		mLifecycleSystem.setScreenDimensions(width, height);
	}
	
	/**
	 * Informs all applicable systems that a touch event has occured.
	 * 
	 * @param event - the touch event.
	 */
	public void onTouch(MotionEvent event) {
		if (mUsePulsar) {
			mPulsarSystem.onTouch(event);
		}
	}
	
	/**
	 * Inform all applicable systems that a sensor event has occured.
	 * 
	 * @param event - the sensor event.
	 */
	public void onSensorEvent(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			mGravitySystem.onOrientationChange(event.values);
			mLifecycleSystem.onOrientationChange(event.values);
		}
	}
	
	/**
	 * Inform all systems that the current drop preference set has changed.
	 */
	public void onPreferencesUpdated() {
		PixelatedPreferences prefs = 
				PixelatedPreferencesManager.getCurrentPreferences();
		
		mUseWind = prefs.windPrefs != null;
		mUsePulsar = prefs.pulsarPrefs != null;
		mUseGravity = prefs.gravityPrefs != null;

		mLifecycleSystem.onPreferencesUpdated();
	}
	
	/**
	 * Inform the system manager whether or not this app is visible to the user.
	 * If not, some system resources are released until the app becomes
	 * visible again.
	 * 
	 * @param context
	 * @param visible
	 */
	public void onVisibilityChanged(Context context, boolean visible) {
		SensorManager sensorManager =
				(SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
		if (visible) {
			if (mUseGravity) {
				Sensor gravitySensor = 
						sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
				sensorManager.registerListener(mSensorListener,
						gravitySensor, SensorManager.SENSOR_DELAY_UI);
			}
		} else {
			if (mUseGravity) {
				Sensor gravitySensor = 
						sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
				sensorManager.unregisterListener(mSensorListener, gravitySensor);
			}
		}
	}
	
	
	private class SystemSensorListener implements SensorEventListener {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			onSensorEvent(event);
		}
		
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// Do nothing
		}		
	}
}
