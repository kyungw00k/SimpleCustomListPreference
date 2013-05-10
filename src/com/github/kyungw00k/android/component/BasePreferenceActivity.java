package com.github.kyungw00k.android.component;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public abstract class BasePreferenceActivity extends PreferenceActivity
		implements OnSharedPreferenceChangeListener {

	public void initializePreference() {
		Map<String, ?> data = getPreferenceManager().getSharedPreferences()
				.getAll();

		for (String key : data.keySet()) {
			onSharedPreferenceChanged(getPreferenceManager()
					.getSharedPreferences(), key);
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		Preference pref = findPreference(key);

		if (pref != null) {
			Object value = sp.getAll().get(key);

			if (pref instanceof ListPreference) {
				ListPreference list_pref = (ListPreference) pref;
				if (value instanceof String) {
					int index = list_pref.findIndexOfValue((String) value);

					if (index >= 0) {
						pref.setSummary(list_pref.getEntries()[index]);
					}
				} else {
					pref.setSummary("" + value);
				}
			} else {
				if (value instanceof String) {
					pref.setSummary("" + value);
				}
			}
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		initializePreference();
	}

	@Override
	protected void onResume() {
		super.onResume();

		//
		// Set up a listener whenever a key changes
		//
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		//
		// Unregister the listener whenever a key changes
		//
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
}
