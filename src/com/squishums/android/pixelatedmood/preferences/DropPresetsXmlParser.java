package com.squishums.android.pixelatedmood.preferences;

/*
 * Possible hierarchy of the drop presets XML is as follows:
 * 
 * <?xml version="1.0" encoding="utf-8"?>
 * <DropPresets>
 *   <DropPrefs
 *     count="_int_"   <!-- Max number of drops on screen at once-->
 *     width="_int_"   <!-- width of the drop image file -->
 *     height="_int_"  <!-- height of the drop image file -->
 *     />
 *   
 *   <PhysicsPrefs                 <!-- Optional -->
 *     veloDampenFactor="_float_"  <!-- [0,1]. Low number => less "drift" -->
 *     />
 *     
 *   <WindPrefs            <!-- Optional -->
 *     angle="_float_"     <!-- Angle of the wind -->
 *     force="_float_"     <!-- Force of the wind -->
 *     forceVar="_float_"  <!-- Variance in the wind force -->
 *     />
 *   
 *   <PulsarPrefs                 <!-- Optional -->
 *     force="_int_"              <!-- Force of the pulse. Negative => push -->
 *     falloffExponent="_float_"  <!-- Affects force vs distance relationship -->
 *     minDistance="_int_"        <!-- Minimum distance for force calc -->
 *     />
 *     
 *   <RenderPrefs
 *     trails="_bool_"     <!-- Adds trails to drops. See "Ash" preset. -->
 *     bitmap1="_string_"  <!-- Relative path to drop image.
 *     ...                        Bitmap should be white. Alpha is respected. -->
 *     bitmap10            <!-- [2,10] are optional. -->
 *     color1="_hexint_"   <!-- Hex color value to apply to bitmap. Alpha is
 *     ...                        ignored. -->
 *     color10             <!-- [2,10] are optional. -->
 *   </DropPresets>
 *   
 *   It is possible to have all systems active at the same time. Each field is
 *   required, unless otherwise stated.
 */


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;


/**
 * Parser for the drop preset XML files. The format and possible options are
 * found in the DropPresetsXmlParser.java file.
 */
public abstract class DropPresetsXmlParser {
	
	private static final String LOG_TAG = DropPresetsXmlParser.class.getName();

	
	/**
	 * Parse the given input stream and return the resulting set as a
	 * PixelatedPreferences object.
	 * 
	 * @param is - input stream of the XML.
	 * @param name - the name to associate the set with.
	 * @param context - context for getting resources.
	 * @return the resulting drop set.
	 */
	public static PixelatedPreferences parse(InputStream is, String name,
			Context context) {
		PixelatedPreferences preferences = null;
		PixelatedPreferences.DropPrefs dropPrefs = null;
		PixelatedPreferences.PhysicsPrefs physicsPrefs = null;
		PixelatedPreferences.WindPrefs windPrefs = null;
		PixelatedPreferences.GravityPrefs gravityPrefs = null;
		PixelatedPreferences.PulsarPrefs pulsarPrefs = null;
		PixelatedPreferences.RenderPrefs renderPrefs = null;
		
		try {
			XmlPullParser parser =
					XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(is, null);
			
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT: {
					preferences = null;
					break;
				}
				
				case XmlPullParser.START_TAG: {
					String tagName = parser.getName();
					if (tagName.equals(PixelatedPreferences
							.DropPrefs.XML_NAME)) {
						dropPrefs = parseDropPrefs(parser);
						
					} else if (tagName.equals(PixelatedPreferences
							.PhysicsPrefs.XML_NAME)) {
						physicsPrefs = parsePhysicsPrefs(parser);
						
					} else if (tagName.equals(PixelatedPreferences
							.WindPrefs.XML_NAME)) {
						windPrefs = parseWindPrefs(parser);
						
					} else if (tagName.equals(PixelatedPreferences
							.GravityPrefs.XML_NAME)) {
						gravityPrefs = parseGravityPrefs(parser);
						
					} else if (tagName.equals(PixelatedPreferences
							.PulsarPrefs.XML_NAME)) {
						pulsarPrefs = parsePulsarPrefs(parser);
						
					} else if (tagName.equals(PixelatedPreferences
							.RenderPrefs.XML_NAME)) {
						renderPrefs = parseRenderPrefs(parser, context);
					}
					break;
				}
				}
				
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			Log.e(LOG_TAG, "Could not instantiate XML parser");
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unhandled IOException");
		}
		
		preferences = new PixelatedPreferences(
				name,
				dropPrefs,
				physicsPrefs,
				windPrefs,
				gravityPrefs,
				pulsarPrefs,
				renderPrefs,
				new Paint());
		
		printLog(name,
				dropPrefs,
				physicsPrefs,
				windPrefs,
				gravityPrefs,
				pulsarPrefs,
				renderPrefs);
		
		return preferences;
	}
	
	/**
	 * Log the success or failure of the parsing.
	 * 
	 * @param name
	 * @param dropPrefs
	 * @param physicsPrefs
	 * @param windPrefs
	 * @param gravityPrefs
	 * @param pulsarPrefs
	 * @param renderPrefs
	 */
	private static void printLog(
			String name,
			PixelatedPreferences.DropPrefs dropPrefs,
			PixelatedPreferences.PhysicsPrefs physicsPrefs,
			PixelatedPreferences.WindPrefs windPrefs,
			PixelatedPreferences.GravityPrefs gravityPrefs,
			PixelatedPreferences.PulsarPrefs pulsarPrefs,
			PixelatedPreferences.RenderPrefs renderPrefs) {
		String log_text = "Parsed XML: " + name + " and got the following fields:  ";
		if (dropPrefs != null) {
			log_text += "DropPrefs, ";
		}
		if (physicsPrefs != null) {
			log_text += "PhysicsPrefs, ";
		}
		if (windPrefs != null) {
			log_text += "WindPrefs, ";
		}
		if (gravityPrefs != null) {
			log_text += "GravityPrefs, ";
		}
		if (pulsarPrefs != null) {
			log_text += "PulsarPrefs, ";
		}
		if (renderPrefs != null) {
			log_text += "RenderPrefs, ";
		}
		
		Log.i(LOG_TAG, log_text.substring(0, log_text.length() - 2));
	}
	
	/* **********************************************************
	 * Section Parsers
	 * **********************************************************/
	
	private static PixelatedPreferences.DropPrefs
			parseDropPrefs(XmlPullParser parser) {
		// Define the fields as strings
		String count = parser.getAttributeValue(null,
				PixelatedPreferences.DropPrefs.XML_COUNT);
		String width = parser.getAttributeValue(null,
				PixelatedPreferences.DropPrefs.XML_WIDTH);
		String height = parser.getAttributeValue(null,
				PixelatedPreferences.DropPrefs.XML_HEIGHT);
		
		PixelatedPreferences.DropPrefs dropPrefs = null;
		try {
			// Convert strings to expected type
			dropPrefs = new PixelatedPreferences.DropPrefs(
					Integer.parseInt(count),
					Integer.parseInt(width),
					Integer.parseInt(height));
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in DropPrefs");
		}
		
		return dropPrefs;
	}
	
	private static PixelatedPreferences.PhysicsPrefs
			parsePhysicsPrefs(XmlPullParser parser) {
		// Define the fields as strings
		String velocityDampeningFactor =
				parser.getAttributeValue(null,
						PixelatedPreferences.PhysicsPrefs
						.XML_VELO_DAMPEN_FACTOR);
		
		PixelatedPreferences.PhysicsPrefs physicsPrefs = null;
		try {
			// Convert strings to expected type
			physicsPrefs = new PixelatedPreferences.PhysicsPrefs(
					Float.parseFloat(velocityDampeningFactor));
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in PhysicsPrefs");
		}
		
		return physicsPrefs;
	}
	
	private static PixelatedPreferences.WindPrefs
			parseWindPrefs(XmlPullParser parser) {
		// Define the fields as strings
		String angle = parser.getAttributeValue(null,
				PixelatedPreferences.WindPrefs.XML_ANGLE);
		String force = parser.getAttributeValue(null,
				PixelatedPreferences.WindPrefs.XML_FORCE);
		String forceVar = parser.getAttributeValue(null,
				PixelatedPreferences.WindPrefs.XML_FORCE_VARIANCE);
		
		PixelatedPreferences.WindPrefs windPrefs = null;
		try {
			// Convert strings to expected type
			windPrefs = new PixelatedPreferences.WindPrefs(
					Float.parseFloat(angle),
					Float.parseFloat(force),
					Float.parseFloat(forceVar));
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in WindPrefs");
		}
		
		return windPrefs;
	}
	
	private static PixelatedPreferences.GravityPrefs
			parseGravityPrefs(XmlPullParser parser) {
		// Define the fields as strings
		String force = parser.getAttributeValue(null,
				PixelatedPreferences.GravityPrefs.XML_FORCE);
		String useZ = parser.getAttributeValue(null,
				PixelatedPreferences.GravityPrefs.XML_USEZ);
		
		PixelatedPreferences.GravityPrefs gravityPrefs = null;
		try {
			// Convert strings to expected type
			gravityPrefs = new PixelatedPreferences.GravityPrefs(
					Float.parseFloat(force),
					Boolean.parseBoolean(useZ));
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in GravityPrefs");
		}
		
		return gravityPrefs;
	}
	
	private static PixelatedPreferences.PulsarPrefs
			parsePulsarPrefs(XmlPullParser parser) {
		// Define the fields as strings
		String force = parser.getAttributeValue(null,
				PixelatedPreferences.PulsarPrefs.XML_FORCE);
		String falloffExponent = parser.getAttributeValue(null,
				PixelatedPreferences.PulsarPrefs
						.XML_FALLOFF_EXPONENT);
		String minDistance = parser.getAttributeValue(null,
				PixelatedPreferences.PulsarPrefs
						.XML_MIN_DISTANCE);
		
		PixelatedPreferences.PulsarPrefs pulsarPrefs = null;
		try {
			// Convert strings to expected type
			pulsarPrefs = new PixelatedPreferences.PulsarPrefs(
					Float.parseFloat(force),
					Float.parseFloat(falloffExponent),
					Float.parseFloat(minDistance));
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in PulsarPrefs");
		}
		
		return pulsarPrefs;
	}
	
	private static PixelatedPreferences.RenderPrefs
			parseRenderPrefs(XmlPullParser parser, Context context) {
		Resources resources = context.getResources();
		PixelatedPreferences.RenderPrefs renderPrefs = null;
		
		int bitmapMax =
				PixelatedPreferences.RenderPrefs.BITMAP_COUNT_MAX;
		List<String> bitmapList = new ArrayList<String>();
		for (int i = 1; i <= bitmapMax; i++) {
			String temp = parser.getAttributeValue(null,
					PixelatedPreferences.RenderPrefs.XML_BITMAP + i);
			if (temp != null) {
				bitmapList.add(temp);
			}
		}
		
		int colorMax =
				PixelatedPreferences.RenderPrefs.COLOR_COUNT_MAX;
		List<String> colorList = new ArrayList<String>();
		for (int i = 1; i <= colorMax; i++) {
			String temp = parser.getAttributeValue(null,
					PixelatedPreferences.RenderPrefs.XML_COLOR + i);
			if (temp != null) {
				colorList.add(temp);
			}
		}
		
		try {
			Bitmap[] bitmaps = new Bitmap[bitmapList.size()];
			for (int i = 0; i < bitmapList.size(); i++) {
				int id = resources.getIdentifier(
						bitmapList.get(i),
						null,
						context.getPackageName());
				bitmaps[i] = BitmapFactory.decodeResource(
						resources, id);
				if (bitmaps[i] == null) {
					throw new Resources.NotFoundException();
				}
			}
		
			Paint[] paints = new Paint[colorList.size()];
			for (int i = 0; i < colorList.size(); i++) {
				int color = (int) Long.parseLong(colorList.get(i), 16);
				paints[i] = new Paint();
				paints[i].setColorFilter(
						new PorterDuffColorFilter(color,
								PorterDuff.Mode.MULTIPLY));
			}
			
			boolean trails = Boolean.parseBoolean(parser.getAttributeValue(null,
					PixelatedPreferences.RenderPrefs.XML_TRAILS));
			
			renderPrefs = new PixelatedPreferences.RenderPrefs(
							bitmaps,
							paints,
							trails);
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "Formatting error in RenderPrefs");
		} catch (Resources.NotFoundException e) {
			Log.e(LOG_TAG, "Bitmap not found");
		}
		
		return renderPrefs;
	}
}