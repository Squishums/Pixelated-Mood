package com.squishums.android.pixelatedmood.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Button;
import android.widget.ListView;

import com.squishums.android.pixelatedmood.R;
import com.squishums.android.pixelatedmood.util.RadioButtonListAdapter;

public class OptionsActivity extends PreferenceActivity {
	
	protected static final String SET_ID_KEY =
			"com.squishums.android.pixelatedmood.setId";
	
	private ListView mListView;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.options_main);
		
    	SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
    	int defaultType = prefs.getInt(
    			getResources().getString(R.string.options_currentDropKey), 0);
    	if (defaultType >= PixelatedPreferencesManager.PRESET_COUNT_START) {
    		defaultType -= PixelatedPreferencesManager.PRESET_COUNT_START;
    	} else {
    		defaultType += PixelatedPreferencesManager.CUSTOM_COUNT;
    	}

    	// Drop type
    	String[] typeTitles =
    			getResources().getStringArray(R.array.options_dropTypes);
		String[][] typeText = new String[typeTitles.length][];
		for (int i = 0; i < typeTitles.length; i++) {
			typeText[i] = new String[]
					{ typeTitles[i] };
		}
		
		mListView = (ListView) this.findViewById(android.R.id.list)
    			.findViewById(android.R.id.list);
		RadioButtonListAdapter adapter =
				new RadioButtonListAdapter(this, typeText, defaultType);
		mListView.setAdapter(adapter);
		
		// Custom set creation isn't implemented yet, so just disable the button.
		Button createSetButton = (Button) this.findViewById(android.R.id.button1);
		createSetButton.setEnabled(false); 
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
    	SharedPreferences.Editor editor = prefs.edit();
    	
    	int selectedItem = ((RadioButtonListAdapter)
				mListView.getAdapter()).getSelectedItem();
    	if (selectedItem >= PixelatedPreferencesManager.CUSTOM_COUNT) {
    		selectedItem -= PixelatedPreferencesManager.CUSTOM_COUNT;
    	} else {
    		selectedItem += PixelatedPreferencesManager.PRESET_COUNT_START;
    	}
    	
    	editor.putInt(getResources().getString(R.string.options_currentDropKey),
    			selectedItem);
    	editor.commit();
    	
    	PixelatedPreferencesManager.setPreferences(this);
    }
}