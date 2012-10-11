package com.squishums.android.pixelatedmood.system;

/*
 * Explanation of the spawn point system:
 * SPAWN_POINT_COUNT denotes the number of spawn points, positioned radially
 * around the center of the screen, slightly inset from the splash bounds.
 * The SPAWN_POINT_ARC is the number of spawn points in use, half to the
 * left of the SpawnAngle and half to the right. The method of choosing
 * a SpawnAngle is different depending on whether the drops use a gravity
 * or wind component.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.FloatMath;
import android.util.Log;

import com.squishums.android.pixelatedmood.component.ComponentPhysics;
import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.component.ComponentRenderable;
import com.squishums.android.pixelatedmood.drop.Drop;
import com.squishums.android.pixelatedmood.drop.DropManager;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferences;
import com.squishums.android.pixelatedmood.preferences.PixelatedPreferencesManager;


/**
 * PixelatedSystem for managing the lifecycle of the drops. The spawning of
 * drops and the culling of off-screen drops is done here. 
 */
public class SystemLifecycle extends PixelatedSystem {

	private static final String LOG_TAG = SystemLifecycle.class.getName();
	// The number of possible spawn points, positioned in a circle around
	// the screen.
	private static final int SPAWN_POINT_COUNT = 300;
	// The number of degrees to spawn drops across.
	private static final int SPAWN_POINT_ARC = 100;
	
	private Random mRandom = new Random();
	// Offscreen drops
	private List<Drop> mDropsToRelease = new ArrayList<Drop>();
	
	// Since gravity affects the spawning positions of drops, we need a reference
	// to the gravity system to get the optimal spawning angle.
	private SystemGravity mGravitySystem;
	
	// Component usage
	private boolean mUsePhysics;
	private boolean mUseWind;
	private boolean mUseGravity;
	
	// Spawn data
	// Current angle at which drops are being spawned.
	private float mSpawnAngle = 0;
	// 
	private int mSpawnAngleIndex = 0;
	// The splash boundaries, indexed the the "x index". The value at each
	// index is the absolute value of the y coordinate of the boundary.
	private float mSplashRadius;
	private float[] mSplashBounds;
	private float[][] mSpawnPoints;
	
	// Screen metrics
	private int mScreenWidth = -1;
	private int mScreenHeight = -1;
	
	// Wind spawn variables
	private float mWindX;
	private float mWindY;
	private float mWindTotal;
	
	// Gravity spawn variables
	private float mGravityForceSign = 1;
	
	
	/**
	 * Creates a new SystemLifecycle using the given gravity system.
	 * @param gravitySystem the gravity system to poll for gravity data.
	 */
	protected SystemLifecycle(SystemGravity gravitySystem) {
		mGravitySystem = gravitySystem;
	}
	
	protected void process(Canvas canvas) {
		// Remove drops outside of the screen bounds
		killDrops();
		// Spawn more drops
		spawnDrops();
	}
	
	/**
	 * Kills all drops that lie outside of the screen dimensions (plus a
	 * small buffer zone.
	 */
	protected void killDrops() {
		DropManager dropManager = Drop.getDropManager();
		for (Drop drop : dropManager.getBoundDrops()) {
			ComponentPosition positionComponent = null;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Drop is lacking a component");
				continue;
			}
			
			if (!pointInBounds(positionComponent.x, positionComponent.y)) {
				// Drop is outside of the screen bounds.
				mDropsToRelease.add(drop);
			}
		}
		
		for (Drop drop : mDropsToRelease) {
			dropManager.releaseDrop(drop);
		}
		mDropsToRelease.clear();
	}
	
	/**
	 * Spawns new drops at the current spawn angle.
	 */
	protected void spawnDrops() {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		Bitmap[] bitmaps = prefs.renderPrefs.bitmaps;
		Paint[] filters = prefs.renderPrefs.filters;
		if (bitmaps.length == 0 || filters.length == 0) {
			// Drops would be invisible, which, at the moment, means they would
			// have no effect, so return.
			return;
		}
		
		// Vary the chance of spawning based on the number of drops. This should
		// probably not be a linear relationship. The chance should never be
		// greater than 1.0, which causes all drops to be created in the first
		// frame (the problem is eventually fixed, as drops die off at different
		// times).
		double chance = prefs.dropPrefs.count / 400.0;
		DropManager dropManager = Drop.getDropManager();
		
		for (int i = dropManager.getDropCount();
				i < prefs.dropPrefs.count; i++) {
			if (mRandom.nextFloat() > chance) { return; }
			
			Drop drop = dropManager.bindDrop();
			
			ComponentPosition positionComponent = null;
			ComponentRenderable renderComponent = null;
			ComponentPhysics physicsComponent = null;
			try {
				positionComponent = drop.getComponent(ComponentPosition.class);
				renderComponent = drop.getComponent(ComponentRenderable.class);
				
				if (mUsePhysics) {
					physicsComponent = 
							drop.getComponent(ComponentPhysics.class);
				}
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Drop is lacking a component");
				continue;
			}
			
			// Render
			renderComponent.bitmap = bitmaps[mRandom.nextInt(bitmaps.length)];
			renderComponent.filter = filters[mRandom.nextInt(filters.length)];
			
			// Position
			if (mUseGravity) {
				// The drops use gravity, so grab a spawn point from any of the
				// spawn points near the SpawnAngleIndex.
				float[] spawn = mSpawnPoints[getSpawnIndex()];
				positionComponent.x = spawn[0];
				positionComponent.y = spawn[1];
			} else {
				// The drops use wind, so grab a spawn point near the wind spawn
				// angle.
				int spawnIndex = mRandom.nextInt(SPAWN_POINT_ARC);
				float[] spawn = mSpawnPoints[spawnIndex];
				positionComponent.x = spawn[0];
				positionComponent.y = spawn[1];
			}
			
			if (mUseWind) {
				setDropWind(physicsComponent);
			}
			
			if (mUsePhysics) {
				setDropPhysics(physicsComponent);
			}
		}
	}
		
	/* ***************************************************************
	 * Lifecycle related methods
	 * ***************************************************************/
	/**
	 * Updates the boundaries for drop removal. This should be called whenever
	 * the screen orientation or resolution changes.
	 */
	private void updateSplashBounds() {	
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		float halfWidth = mScreenWidth / 2;
		float halfHeight = mScreenHeight / 2;
		float dropSize = Math.max(
				prefs.dropPrefs.width,
				prefs.dropPrefs.height);
		
		mSplashRadius = FloatMath.sqrt(
				halfWidth*halfWidth + halfHeight*halfHeight) +
				2 * dropSize;
		mSplashBounds = new float[(int) (2 * mSplashRadius)];
		
		for (int x = 0; x < (int) (2 * mSplashRadius); x++) {
			// Recalculate the spawn points.
			float realX = x - mSplashRadius;
			float bound = FloatMath.sqrt(
					mSplashRadius*mSplashRadius
					- realX*realX);
			mSplashBounds[x] = bound;
		}
	}
	
	/**
	 * Checks if the given x, y point is within the splash boundaries.
	 * 
	 * @param x
	 * @param y
	 * @return true if the point is within the splash boundaries.
	 */
	private boolean pointInBounds(float x, float y) {
		final int index = xToIndex(x);
		final int upperBound = (int) (2 * mSplashRadius);
		
		if (index >= 0 && index < upperBound) {
			return (mSplashBounds[index] > Math.abs(y - mScreenHeight / 2));
		}
		return false;
	}
	
	/**
	 * Converts a x screen coordinate into a splash boundary index.
	 * 
	 * @param x - x portion of a screen coordinate (value may correspond with
	 * 		a value that is outside of the screen bounds).
	 * @return the splash boundary index for that screen coordinate.
	 */
	private int xToIndex(float x) {
		return (int) (x + (mSplashRadius - mScreenWidth / 2));
	}
	
	
	/* ***************************************************************
	 * Wind
	 * ***************************************************************/
	/**
	 * Recalculates the spawn points based on the wind.
	 */
	private void updateWindSpawnPoints() {
		if (mSpawnPoints == null || mSpawnPoints.length != SPAWN_POINT_ARC) {
			mSpawnPoints = new float[SPAWN_POINT_ARC][2];
		}
		
		float radius = mSplashRadius - 3;
		float step = (float) ((2 * Math.PI) / SPAWN_POINT_COUNT);
		float angle = mSpawnAngle
				- step * SPAWN_POINT_ARC / 2;

		for (int i = 0; i < SPAWN_POINT_ARC; i++) {
			float x = -FloatMath.cos(angle) * radius + mScreenWidth / 2;
			float y = FloatMath.sin(angle) * radius + mScreenHeight / 2;
			mSpawnPoints[i][0] = x;
			mSpawnPoints[i][1] = y;
			
			angle += step;
		}
	}
	
	/**
	 * Recalculates some internal wind force-related variables. 
	 * 
	 * @param physicsComponent - physicsComponent of the current drop
	 * 		preferences set.
	 */
	private void setDropWind(ComponentPhysics physicsComponent) {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		float windVariance = (2 * mRandom.nextFloat() - 1)
				* prefs.windPrefs.forceVariance;
		float windVarianceX = (mWindX / mWindTotal) * windVariance;
		float windVarianceY = (mWindY / mWindTotal) * windVariance;
		physicsComponent.windFactorX = mWindX + windVarianceX;
		physicsComponent.windFactorY = mWindY + windVarianceY;
	}
	
	/**
	 * Updates the wind angle and force.
	 */
	private void updateWind() {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		mSpawnAngle = prefs.windPrefs.angle;
		mWindX = prefs.windPrefs.force
				* FloatMath.cos(prefs.windPrefs.angle);
		mWindY = prefs.windPrefs.force
				* -FloatMath.sin(prefs.windPrefs.angle);
		
		mWindTotal = FloatMath.sqrt(mWindX*mWindX + mWindY*mWindY);
	}
	
	/* ***************************************************************
	 * Gravity
	 * ***************************************************************/
	/**
	 * Updates the gravity related spawn.
	 * 
	 * @param vector - 3D vector from the gravity sensor. [x, y, z]
	 */
	protected void onOrientationChange(float[] vector) {
		mSpawnAngle = (float) (mGravitySystem.getGravityAngleXY());
		mSpawnAngle += (mGravityForceSign > 0) ? Math.PI : 0;
		
		mSpawnAngleIndex = (int)
				((SPAWN_POINT_COUNT * (mSpawnAngle / (Math.PI * 2))
						% SPAWN_POINT_COUNT));
		mSpawnAngleIndex += (mSpawnAngleIndex < 0) ? SPAWN_POINT_COUNT : 0;
	}
	
	/* ***************************************************************
	 * Physics
	 * ***************************************************************/
	/**
	 * Initialize a drop's physics component.
	 * 
	 * @param physicsComponent - a drop's physics component.
	 */
	private void setDropPhysics(ComponentPhysics physicsComponent) {
		physicsComponent.veloX = 0;
		physicsComponent.veloY = 0;
	}
	
	/* ***************************************************************
	 * Misc.
	 * ***************************************************************/
	/**
	 * Updates the spawn points used by both the gravity and wind systems.
	 */
	private void updateStaticSpawnPoints() {
		if (mSpawnPoints == null || mSpawnPoints.length != SPAWN_POINT_COUNT) {
			mSpawnPoints = new float[SPAWN_POINT_COUNT][2];
		}
		
		final float radius = mSplashRadius - 3;
		final float step = (float) ((2 * Math.PI) / SPAWN_POINT_COUNT);
		float angle = 0;
		
		for (int i = 0; i < SPAWN_POINT_COUNT; i++) {
			float x = -FloatMath.cos(angle) * radius + mScreenWidth / 2;
			float y = FloatMath.sin(angle) * radius + mScreenHeight / 2;
			mSpawnPoints[i][0] = x;
			mSpawnPoints[i][1] = y;
			
			angle += step;
		}
	}
	
	/**
	 * Returns a random, valid spawn index. Use this to get a spawn point for a
	 * drop.
	 * 
	 * @return a random spawn index.
	 */
	private int getSpawnIndex() {
		int spawnIndex = mSpawnAngleIndex
				+ (mRandom.nextInt(SPAWN_POINT_ARC)
						- SPAWN_POINT_ARC / 2);
		spawnIndex = spawnIndex % SPAWN_POINT_COUNT;
		spawnIndex = (spawnIndex < 0)
				? spawnIndex + SPAWN_POINT_COUNT : spawnIndex;
		
		return spawnIndex;
	}
	
	/**
	 * Update the screen dimensions used for spawning drops. This hould be
	 * called whenever the screen orientation or resolution changes.
	 * 
	 * @param width
	 * @param height
	 */
	protected void setScreenDimensions(int width, int height) {
		boolean change = mScreenWidth != width || mScreenHeight != height;
		mScreenWidth = width;
		mScreenHeight = height;
		
		if (change) {
			updateSplashBounds();
			if (mUseGravity) {
				updateStaticSpawnPoints();
			} else {
				updateWindSpawnPoints();
			}
		}
	}
	
	/**
	 * Update the drop set being used by the spawn system. This should be
	 * 	called whenever the current drop set changes.
	 */
	protected void onPreferencesUpdated() {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		checkLoadableComponents();
		if (mUseWind) {
			updateWind();
		}
		
		updateSplashBounds();
		if (mUseGravity) {
			mGravityForceSign =
					Math.signum(prefs.gravityPrefs.force);
			updateStaticSpawnPoints();
		} else {
			updateWindSpawnPoints();
		}
	}
	
	/**
	 * Determines whether or not to preform logic related to different drop
	 * set components.
	 */
	private void checkLoadableComponents() {
		PixelatedPreferences prefs =
				PixelatedPreferencesManager.getCurrentPreferences();
		
		mUseWind = prefs.windPrefs != null;
		mUseGravity = prefs.gravityPrefs != null;
		mUsePhysics = mUseWind
				|| prefs.pulsarPrefs != null;
	}
}
