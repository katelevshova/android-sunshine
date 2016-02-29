package com.hally.sunshine.view;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.hally.sunshine.R;

/**
 * Created by Kateryna Levshova on 29.02.2016.
 */
public class SettingsActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
					.add(R.id.settings_container, new SettingsFragment()).commit();
		}
	}
}
