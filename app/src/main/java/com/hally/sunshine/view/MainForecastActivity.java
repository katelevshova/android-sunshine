package com.hally.sunshine.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.hally.sunshine.R;
import com.hally.sunshine.util.TraceUtil;


public class MainForecastActivity extends Activity
{
	private final String CLASS_NAME = MainForecastActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
					.add(R.id.container, new MainForecastFragment())
					.commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.item_settings)
		{
			startActivity(new Intent(this, SettingsDetailActivity.class));
			return true;
		}

		if(id == R.id.item_map)
		{
			openPreferredLocationMap();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void openPreferredLocationMap()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String location = preferences.getString(getString(R.string.pref_location_key),
				getString(R.string.pref_location_default));

		// Using the URI scheme for showing a location found on a map. This super-handy
		// intent can is detailed in the "Common Intents" page of Android's developer site:
		// http://developer.android.com/guide/components/intents-common.html#Maps

		Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",
				location).build();

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geoLocation);

		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivity(intent);
		}
		else
		{
			TraceUtil.logD(CLASS_NAME, "openPreferredLocationMap",
					"Couldn't call " + location + ", no receiving apps installed!");
		}

	}
}
