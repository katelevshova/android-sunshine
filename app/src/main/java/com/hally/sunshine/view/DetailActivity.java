package com.hally.sunshine.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hally.sunshine.R;


public class DetailActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		if (savedInstanceState == null)
		{
			Bundle args = new Bundle();
			args.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

			DetailFragment detailFragmen = new DetailFragment();
			detailFragmen.setArguments(args);

			getSupportFragmentManager().beginTransaction()
					.add(R.id.detail_container, detailFragmen).commit();
		}

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//		{
//			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//			setSupportActionBar(toolbar);
//			// fix for back arrow button
//			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//			getSupportActionBar().setDisplayShowHomeEnabled(true);
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
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
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
