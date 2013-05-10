package com.example.activity;

import android.os.Bundle;
import android.view.Menu;

import com.github.kyungw00k.component.android.BasePreferenceActivity;

public class MainActivity extends BasePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//
		// Load Preferences
		//
		addPreferencesFromResource(R.xml.example_preference);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
