package com.hally.sunshine.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

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

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//		{
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			// fix for back arrow button
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
//		}
	}
}
