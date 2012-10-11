package com.squishums.android.pixelatedmood.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squishums.android.pixelatedmood.R;


/**
 * A custom list adapter for RadioButtons, the layout for which is defined in
 * options_radio_prefernce.xml. The entire list will act like one radio group.
 */
public class RadioButtonListAdapter extends ArrayAdapter<String[]> {

	// A list of titles and summaries of the radio button. The first index in
	// the string array is the title, the second (if it exists) is the summary.
	private List<String[]> mTitles = new ArrayList<String[]>();
	// The radio buttons in the list.
	private List<RadioButton> mRadioGroup = new ArrayList<RadioButton>();
	// The index of the currently selected radio button.
	private int mSelected;
	
	
	/**
	 * Creates a RadioButtonListAdapter from the given data.
	 * 
	 * @param context - the context to use for accessing resources.
	 * @param data - an array of string arrays. The first element in the string
	 * 		array
	 * @param initValue
	 */
	public RadioButtonListAdapter(Context context,
			String[][] data, int initValue) {
		super(context, R.layout.options_radio_preference,
				android.R.id.title, data);
		
		mTitles = new ArrayList<String[]>();
		mTitles.addAll(Arrays.asList(data));
		mSelected = initValue;
	}
	
	/**
	 * Not yet implemented.
	 */
	@Override
	public void add(String[] item) { }
	
	/**
	 * Not yet implemented.
	 */
	@Override
	public void remove(String[] item) { }

	/**
	 * Returns the item at position.
	 * 
	 * @return the item at position.
	 */
	@Override
	public String[] getItem(int position) {
		return mTitles.get(position);
	}

	/**
	 * Not yet implemented.
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/**
	 * Returns the index of the specified item, or -1 if the item was not found.
	 * 
	 * return the index of the item, or -1 if not found.
	 */
	@Override
	public int getPosition(String[] item) {
		return mTitles.indexOf(item);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup newParent) {
		RelativeLayout rowView = (RelativeLayout) convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = (RelativeLayout) inflater.inflate(
					R.layout.options_radio_preference, newParent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.position = position;
			holder.titleView =
					(TextView) rowView.findViewById(R.id.options_title);
			holder.summaryView =
					(TextView) rowView.findViewById(R.id.options_summary);
			holder.radioButton =
					(RadioButton) rowView.findViewById(R.id.options_radioButton);

			mRadioGroup.add(holder.radioButton);
			
			rowView.setTag(holder);
			rowView.setOnClickListener(getClickListener());
		}
		
		updateView(rowView, position);
		
		return rowView;
	}
	
	/**
	 * Returns the position of the selected Radio Button.
	 * @return
	 */
	public int getSelectedItem() {
		return mSelected;
	}

	/**
	 * Updates the RadioButton to match its tag data.
	 * 
	 * @param rowView - the view of the radio button to update.
	 * @param position - the position of the radio button.
	 */
	private void updateView(RelativeLayout rowView, int position) {
		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.position = position;
		// Update the checkbox
		holder.radioButton.setChecked(position == mSelected);
		
		// Update the text;
		String[] textData = mTitles.get(position);
		
		if (textData.length > 0) {
			holder.titleView.setText(textData[0]);
		}
		
		if (textData.length > 1) {
			holder.summaryView.setText(textData[1]);
			holder.summaryView.setVisibility(View.VISIBLE);
		} else {
			holder.summaryView.setVisibility(View.GONE);
		}
	}
	
	private OnClickListener getClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewHolder holder = (ViewHolder) view.getTag();
				if (!holder.radioButton.isChecked()) {
					mSelected = holder.position;
					holder.radioButton.setChecked(true);
					for (RadioButton radio : mRadioGroup) {
						radio.setChecked(radio == holder.radioButton);
					}
				}
			}
		};
	}
	
	/**
	 * Data holder for the radio buttons.
	 */
	private class ViewHolder {
		public int position;
		public TextView titleView;
		public TextView summaryView;
		public RadioButton radioButton;
	}
}
