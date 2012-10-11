package com.squishums.android.pixelatedmood.preferences;

import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squishums.android.pixelatedmood.R;


/**
 * Manager for interactions with the PixelatedPreferences. Provides methods to
 * access the current drop set. To change the current drop set, change the value
 * in the DefaultSharedPreferences.
 */
public abstract class PixelatedPreferencesManager {
	
	private static final String LOG_TAG =
			PixelatedPreferencesManager.class.getName();
	
	// Start of the ID number for presets. Useless at the moment, but will be
	// handy if custom sets are ever allowed.
	protected static final int PRESET_COUNT_START = 100;
	protected static final int PRESET_COUNT = 5;
	protected static final int CUSTOM_COUNT = 0;
	
	private static PixelatedPreferences[] mPresets =
			new PixelatedPreferences[PRESET_COUNT];
	// Whether or not the presets have been loaded into memory.
	private static boolean mValuesLoaded = false;
	
	// Current drop set
	private static PixelatedPreferences mCurrentPrefs;
	
	
	/**
	 * Load the drop presets into memory. This should be called before trying
	 * to access any drop sets' data or set the current drop set.
	 *  
	 * @param context
	 */
	public static void loadPresets(Context context) {
		// Might be worthwhile to parse the data on-demand, as changing the drop
		// set is done fairly infrequently, so there's no need to keep track of
		// every set at once.
		
		// Snow
		InputStream is = context.getResources()
				.openRawResource(R.raw.drop_presets_snowflake);
		mPresets[0] = DropPresetsXmlParser.parse(is, "Snowflake", context);
		
		// Rain
		is = context.getResources()
				.openRawResource(R.raw.drop_presets_raindrop);
		mPresets[1] = DropPresetsXmlParser.parse(is, "Raindrop", context);
		
		// Shapes
		is = context.getResources()
				.openRawResource(R.raw.drop_presets_shape);
		mPresets[2] = DropPresetsXmlParser.parse(is, "Shape", context);
		
		// Bubbles
		is = context.getResources()
				.openRawResource(R.raw.drop_presets_orb);
		mPresets[3] = DropPresetsXmlParser.parse(is, "Orb", context);
		// Currently, there's no XML syntax to add shaders to the background,
		// so we have to hardcode that here.
		mPresets[3].backgroundColor.setShader(new LinearGradient(
				0, 640,
				0, 0,
				0xFF000055, 0xFF000000,
				Shader.TileMode.CLAMP));
		
		// Ashes
		is = context.getResources()
				.openRawResource(R.raw.drop_presets_ash);
		mPresets[4] = DropPresetsXmlParser.parse(is, "Ash", context);
		mPresets[4].backgroundColor.setColor(0xFFDDDDDD);
		
		mValuesLoaded = true;
		
		setPreferences(context);
	}
	
	/**
	 * Update the current drop set to match the one stored in SharedPreferences.
	 * To change the drop set, change that number and call this method. If the
	 * presets haven't been loaded, this function returns null.
	 * 
	 * @param context
	 * @return the new current drop set, or null if presets haven't been loaded.
	 */
	public static PixelatedPreferences setPreferences(Context context) {
		// This is really silly. This function should take an optional argument
		// to set the new set number 
		if (!mValuesLoaded) {
			Log.d(LOG_TAG, "Load presets before attempting to access data");
			return null;
		}
				
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int setId = prefs.getInt(
				context.getResources().getString(R.string.options_currentDropKey),
				0);
		
		if (setId >= CUSTOM_COUNT) {
			mCurrentPrefs = mPresets[setId - PRESET_COUNT_START];
		} else {
			Log.e(LOG_TAG, "Could not set preferences to set " + setId
					+ ". Max " + CUSTOM_COUNT + ".");
		}
		
		return mCurrentPrefs;
	}
	
	/**
	 * Returns the current drop set, or null if the presets haven't been loaded.
	 * @return
	 */
	public static PixelatedPreferences getCurrentPreferences() {
		if (!mValuesLoaded) {
			Log.d("LOG_TAG", "Load presets before attempting to access data");
			return null;
		}
		
		return mCurrentPrefs;
	}
}
