package com.squishums.android.pixelatedmood.preferences;

/*
 * WARNING: Each preference set should only implement either a wind or gravity
 * component. There are non-fatal issues with the spawning system when both are
 * in use at the same time.
 * 
 * No integrity checks are done. You have been warned.
 */

import android.graphics.Bitmap;
import android.graphics.Paint;


/**
 * Container for the preferences associated with a drop set. 
 */
public class PixelatedPreferences {
	
	// Required Non-Package Data
	public final String name;
	public final Paint backgroundColor;
	
	// Optional Packages
	public final PhysicsPrefs physicsPrefs;
	public final WindPrefs windPrefs;
	public final GravityPrefs gravityPrefs;
	public final PulsarPrefs pulsarPrefs;
	
	// Required Packages
	public DropPrefs dropPrefs;
	public RenderPrefs renderPrefs;
	

	protected PixelatedPreferences(
			String name,
			DropPrefs dropPrefs,
			PhysicsPrefs physicsPrefs,
			WindPrefs windPrefs,
			GravityPrefs gravityPrefs,
			PulsarPrefs pulsarPrefs,
			RenderPrefs renderPrefs,
			Paint backgroundColor) {
		this.name = name;
		this.dropPrefs = dropPrefs;
		this.physicsPrefs = physicsPrefs;
		this.windPrefs = windPrefs;
		this.gravityPrefs = gravityPrefs;
		this.pulsarPrefs = pulsarPrefs;
		this.renderPrefs = renderPrefs;
		this.backgroundColor = backgroundColor;
	}
	
	
	/** Drop preferences. **/
	public static class DropPrefs {
		
		// XML Tags
		protected static final String XML_NAME = "DropPrefs";
		protected static final String XML_COUNT = "count";
		protected static final String XML_WIDTH = "width";
		protected static final String XML_HEIGHT = "height";
		
		/** Maximum number of drops on the screen at once. **/
		public final int count;
		/** Width of the drop's bitmap. **/   // Move to render prefs?
		public final int width;
		/** Height of the drop's bitmap. **/  // Move to render prefs?
		public final int height;
		
		
		protected DropPrefs(
				int count,
				int width,
				int height) {
			this.count = count;
			this.width = width;
			this.height = height;
		}
	}
	
	
	/** Physics preference. **/
	public static class PhysicsPrefs {
		
		// XML Tags
		protected static final String XML_NAME = "PhysicsPrefs";
		protected static final String XML_VELO_DAMPEN_FACTOR = "veloDampenFactor";
		
		/** The factor to multiply each drop's current velocity by each frame. **/
		public final float velocityDampeningFactor;
		
		
		protected PhysicsPrefs(
				float velocityDampeningFactor) {
			this.velocityDampeningFactor = velocityDampeningFactor;
		}
	}
	
	
	/** Wind (constant drop movement) preferences. **/
	public static class WindPrefs {

		// XML Tags
		protected static final String XML_NAME = "WindPrefs";
		protected static final String XML_ANGLE = "angle";
		protected static final String XML_FORCE = "force";
		protected static final String XML_FORCE_VARIANCE = "forceVar";
		
		/** Angle of the wind, in radians. **/
		public final float angle;
		/** How strongly the wind affects the drops. **/
		public final float force;
		/** The maximum absolute variance in wind force applied to each drop. **/ 
		public final float forceVariance;
		
		
		protected WindPrefs(
				float angle,
				float force,
				float forceVariance) {
			this.angle = angle;
			this.force = force;
			this.forceVariance = forceVariance;
		}
	}
	
	
	/** Gravity induced movement preferences. **/
	public static class GravityPrefs {

		// XML Tags
		protected static final String XML_NAME = "GravityPrefs";
		protected static final String XML_FORCE = "force";
		protected static final String XML_USEZ = "useZ";
		
		/** How strongly gravity affects the drop's movements. + or - **/
		public final float force;
		/** Whether to consider the Z axis in force calculations. **/ 
		public final boolean useZ;
		
		
		protected GravityPrefs(
				float force,
				boolean useZ) {
			this.force = force;
			this.useZ = useZ;
		}
	}
	
	
	/** Touch induced movement preferences. **/
	public static class PulsarPrefs {
		
		// XML Tags
		protected static final String XML_NAME = "PulsarPrefs";
		protected static final String XML_FORCE = "force";
		protected static final String XML_FALLOFF_EXPONENT = "falloffExponent";
		protected static final String XML_MIN_DISTANCE = "minDistance";
		
		/** Force applied to drops from touch points. Negative => push. **/
		public final float force;
		/** How quickly the force diminishes with distance. ~1.x **/
		public final float falloffExponent;
		/** Minimum distance from touch to drop to use for force calculations. **/
		public final float minDistance;
		
		
		protected PulsarPrefs(
				float force,
				float falloffExponent,
				float minDistance) {
			this.force = force;
			this.falloffExponent = falloffExponent;
			this.minDistance = minDistance;
		}
	}
	
	
	/** Drop rendering preferences. **/
	public static class RenderPrefs {

		// XML Tags
		protected static final String XML_NAME = "RenderPrefs";
		protected static final String XML_BITMAP = "bitmap";
		protected static final String XML_COLOR = "color";
		protected static final String XML_TRAILS = "trails";
		
		protected static final int BITMAP_COUNT_MAX = 10;
		protected static final int COLOR_COUNT_MAX = 10;
		
		/** Array of bitmaps to randomly select from. **/
		public final Bitmap[] bitmaps;
		/** Paint filters too apply to the drop bitmaps **/
		public final Paint[] filters;
		/** Whether trails appear behind the drops. **/
		public final boolean trails;
		
		
		protected RenderPrefs(
				Bitmap[] bitmaps,
				Paint[] filters,
				boolean trails) {
			this.bitmaps = bitmaps;
			this.filters = filters;
			this.trails = trails;
		}
	}
}
