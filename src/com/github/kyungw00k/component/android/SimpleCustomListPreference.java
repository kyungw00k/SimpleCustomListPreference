package com.github.kyungw00k.component.android;

import com.example.activity.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleCustomListPreference extends ListPreference {
	private static final String TAG = "SimpleCustomListPreference";

	private Context mContext;
	private LayoutInflater mInflater;
	private CharSequence[] entries;
	private CharSequence[] entryValues;
	private CustomListPreferenceAdapter customListPreferenceAdapter = null;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private String currentKey;

	public SimpleCustomListPreference(Context context, AttributeSet set) {
		super(context, set);

		mContext = context;
		mInflater = LayoutInflater.from(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		editor = prefs.edit();

		currentKey = getKey();

		entries = loadArray("entries", mContext);
		entryValues = loadArray("entryValues", mContext);

		if (entries != null) {
			setEntries(entries);
			setEntryValues(entryValues);
		}
	}

	private String[] loadArray(String arrayName, Context mContext) {
		int size = prefs.getInt(currentKey + "_" + arrayName + "_size", 0);
		String array[] = null;

		if (size > 0) {
			editor.clear();
			array = new String[size];

			for (int i = 0; i < size; i++) {
				array[i] = prefs.getString(currentKey + "_" + arrayName + "_"
						+ i, null);
			}
		}

		return array;
	}

	private boolean saveArray(CharSequence[] array, String arrayName,
			Context mContext) {
		editor.putInt(currentKey + "_" + arrayName + "_size", array.length);

		for (int i = 0; i < array.length; i++) {
			editor.remove(currentKey + "_" + arrayName + "_" + i);
			editor.putString(currentKey + "_" + arrayName + "_" + i,
					(String) array[i]);
		}
		return editor.commit();
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);

		entries = loadArray("entries", mContext);
		entryValues = loadArray("entryValues", mContext);

		if (entries == null) {
			entries = getEntries();
			entryValues = getEntryValues();
			saveArray(entries, "entries", mContext);
			saveArray(entryValues, "entryValues", mContext);
		}

		if (entries == null || entryValues == null
				|| entries.length != entryValues.length) {
			throw new IllegalStateException(
					"SimpleCustomListPreference requires an entries array and an entryValues array which are both the same length");
		}

		Object value = getValue();

		if (value == null) {
			value = entryValues[0];
		}

		int index = findIndexOfValue(getSharedPreferences().getString(
				currentKey, (String) value));

		customListPreferenceAdapter = new CustomListPreferenceAdapter(index);

		builder.setAdapter(customListPreferenceAdapter, this);
	}

	private class CustomListPreferenceAdapter extends BaseAdapter {
		final int selected;

		public CustomListPreferenceAdapter(int index) {
			selected = index;
		}

		@Override
		public int getCount() {
			return entries.length;
		}

		@Override
		public Object getItem(int position) {
			return entryValues[position];

		}

		@Override
		public long getItemId(int position) {
			return position;

		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View row = mInflater.inflate(R.layout.list_view, null);

			new CustomHolder(row, position, selected);

			row.setClickable(true);
			row.setId(position);

			if (position == selected) {
				row.setSelected(true);
			}

			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String value = (String) entryValues[arg0.getId()];
					editor = prefs.edit();
					editor.putString(currentKey, value + "");
					if (editor.commit()) {
						arg0.setSelected(true);

						//
						// Mark as selected
						//
						setValue((String) entryValues[arg0.getId()]);
					}

					getDialog().dismiss();
				}
			});

			return row;
		}
	}

	private class CustomHolder {
		TextView text = null;
		ImageButton btnAdd = null;
		ImageButton btnModify = null;
		ImageButton btnDelete = null;

		CustomHolder(View row, int position, int selected) {
			text = (TextView) row.findViewById(R.id.text_title);

			if (text != null) {
				text.setText(entries[position]);

				if (position == selected) {
					text.setTypeface(null, Typeface.BOLD);
					text.setSelected(true);
				}
			}

			btnAdd = (ImageButton) row.findViewById(R.id.btn_add);

			btnModify = (ImageButton) row.findViewById(R.id.btn_modify);

			if (btnModify != null) {
				btnModify.setId(position);
				btnModify.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						final LinearLayout addItemView = (LinearLayout) View
								.inflate(mContext, R.layout.list_additem, null);
						final int currentPosition = v.getId();

						EditText keyText = (EditText) addItemView
								.findViewById(R.id.listItemKey);
						keyText.setText(entries[currentPosition]);

						EditText valueText = (EditText) addItemView
								.findViewById(R.id.listItemValue);
						valueText.setText(entryValues[currentPosition]);

						new AlertDialog.Builder(mContext)
								.setView(addItemView)
								.setTitle("수정할 내용을 입력하세요")
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												EditText keyText = (EditText) addItemView
														.findViewById(R.id.listItemKey);

												EditText valueText = (EditText) addItemView
														.findViewById(R.id.listItemValue);

												if (keyText.getText().length() == 0) {
													Toast.makeText(
															mContext,
															R.string.list_item_title_empty,
															Toast.LENGTH_SHORT)
															.show();
													keyText.requestFocus();
													return;
												}

												if (valueText.getText()
														.length() == 0) {
													Toast.makeText(
															mContext,
															R.string.list_item_value_empty,
															Toast.LENGTH_SHORT)
															.show();
													valueText.requestFocus();
													return;
												}

												entries[currentPosition] = keyText
														.getText().toString();
												entryValues[currentPosition] = valueText
														.getText().toString();

												setEntries(entries);
												setEntryValues(entryValues);

												if (saveArray(entries,
														"entries", mContext)
														&& saveArray(
																entryValues,
																"entryValues",
																mContext)) {

													entries = loadArray(
															"entries", mContext);
													entryValues = loadArray(
															"entryValues",
															mContext);

													setEntries(entries);
													setEntryValues(entryValues);

													customListPreferenceAdapter
															.notifyDataSetChanged();
													customListPreferenceAdapter
															.notifyDataSetInvalidated();
												}
											}
										})
								.setNegativeButton(android.R.string.cancel,
										null).show();

					}
				});
			}

			btnDelete = (ImageButton) row.findViewById(R.id.btn_delete);

			if (btnDelete != null) {
				btnDelete.setId(position);
				btnDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (entries.length == 1) {
							Toast.makeText(mContext,
									R.string.list_item_delete_error,
									Toast.LENGTH_SHORT).show();
							return;
						}

						CharSequence[] key = new String[entries.length - 1];
						CharSequence[] value = new String[entries.length - 1];

						for (int idx = 0, pos = 0, len = entries.length; pos != len; ++pos) {
							if (v.getId() == pos) {
								continue;
							}
							key[idx] = entries[pos];
							value[idx] = entryValues[pos];
							idx++;
						}
						entries = key;
						entryValues = value;

						if (saveArray(key, "entries", mContext)
								&& saveArray(value, "entryValues", mContext)) {

							setEntries(loadArray("entries", mContext));
							setEntryValues(loadArray("entryValues", mContext));

							customListPreferenceAdapter.notifyDataSetChanged();
							customListPreferenceAdapter
									.notifyDataSetInvalidated();
						}

					}
				});
			}

			if (btnAdd != null) {
				if (position + 1 == entries.length) {
					btnAdd.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							final LinearLayout addItemView = (LinearLayout) View
									.inflate(mContext, R.layout.list_additem,
											null);

							new AlertDialog.Builder(mContext)
									.setView(addItemView)
									.setTitle(R.string.list_item_add)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {

													CharSequence[] key = new CharSequence[entries.length + 1];
													CharSequence[] value = new CharSequence[entries.length + 1];

													for (int pos = 0, len = entries.length; pos != len; ++pos) {
														key[pos] = entries[pos];
														value[pos] = entryValues[pos];
													}

													EditText keyText = (EditText) addItemView
															.findViewById(R.id.listItemKey);

													EditText valueText = (EditText) addItemView
															.findViewById(R.id.listItemValue);

													if (keyText.getText()
															.length() == 0) {
														Toast.makeText(
																mContext,
																R.string.list_item_title_empty,
																Toast.LENGTH_SHORT)
																.show();
														keyText.requestFocus();
														return;
													}

													if (valueText.getText()
															.length() == 0) {
														Toast.makeText(
																mContext,
																R.string.list_item_value_empty,
																Toast.LENGTH_SHORT)
																.show();
														valueText
																.requestFocus();
														return;
													}

													key[entries.length] = keyText
															.getText()
															.toString();
													value[entries.length] = valueText
															.getText()
															.toString();

													if (saveArray(key,
															"entries", mContext)
															&& saveArray(
																	value,
																	"entryValues",
																	mContext)) {

														entries = loadArray(
																"entries",
																mContext);
														entryValues = loadArray(
																"entryValues",
																mContext);

														setEntries(entries);
														setEntryValues(entryValues);

														customListPreferenceAdapter
																.notifyDataSetChanged();
														customListPreferenceAdapter
																.notifyDataSetInvalidated();
													}
												}
											})
									.setCancelable(true)
									.setNegativeButton(android.R.string.cancel,
											null).show();

						}
					});
					btnAdd.setVisibility(View.VISIBLE);
				} else {
					btnAdd.setVisibility(View.GONE);
				}
			}

			if (entries.length == 1 && btnDelete != null) {
				btnDelete.setVisibility(View.GONE);
			}
		}
	}
}
