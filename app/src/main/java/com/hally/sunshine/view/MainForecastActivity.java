package com.hally.sunshine.view;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hally.sunshine.R;
import com.hally.sunshine.model.IForecastFragmentCallback;
import com.hally.sunshine.sync.SunshineSyncAdapter;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.Util;


public class MainForecastActivity extends ActionBarActivity implements IForecastFragmentCallback
{
	private final String CLASS_NAME = MainForecastActivity.class.getSimpleName();
	private static final String DETAILFRAGMENT_TAG = "DFTAG";
	private String _location;
	private boolean _hasTwoPane = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_location = Util.getPreferredLocation(this);

		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			ActionBar actionBar = getSupportActionBar();
		}

		if (findViewById(R.id.detail_container) != null)
		{
			// The detail container view will be present only in the large-screen layouts
			// (res/layout-sw600dp). If this view is present, then the activity should be
			// in two-pane mode.
			_hasTwoPane = true;
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			if (savedInstanceState == null)
			{
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
						.commit();
			}
		}
		else
		{
			_hasTwoPane = false;
//			getSupportActionBar().setElevation(0f); // removes a shadow under the ActionBar for phone
		}

		getSupportActionBar().setIcon(R.mipmap.art_clear);
		setUseTodayItemElement();

		SunshineSyncAdapter.initializeSyncAdapter(this);
	}

	private void setUseTodayItemElement()
	{
		MainForecastFragment mainForecastFragment =
				(MainForecastFragment) getSupportFragmentManager()
						.findFragmentById(R.id.fragment_main);

		mainForecastFragment.setUseTodayItem(!_hasTwoPane);
	}

	public boolean getHasTwoPane()
	{
		return _hasTwoPane;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main_f_activity, menu);

		boolean isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
		menu.findItem(R.id.item_db_manager).setVisible(isDebuggable);
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

		if(id == R.id.item_db_manager)
		{
			startActivity(new Intent(this, AndroidDatabaseManager.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onResume()
	{
		super.onResume();
		String location = Util.getPreferredLocation(this);

		// update the location in our second pane using the fragment manager
		if (location != null && !location.equals(_location))
		{
			MainForecastFragment mainForecastFragment =
					(MainForecastFragment) getSupportFragmentManager()
							.findFragmentById(R.id.fragment_main);

			if (mainForecastFragment != null)
			{
				mainForecastFragment.onLocationChanged();
			}

			DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
					.findFragmentByTag(DETAILFRAGMENT_TAG);

			if (detailFragment != null)
			{
				detailFragment.onLocationChanged(location);
			}

			_location = location;
		}
	}

	@Override
	public void onItemSelected(Uri dateUri)
	{
		if (_hasTwoPane)
		{
			Bundle args = new Bundle();
			args.putParcelable(DetailFragment.DETAIL_URI, dateUri);

			DetailFragment detailFragment = new DetailFragment();
			detailFragment.setArguments(args);

			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.detail_container, detailFragment,
					DETAILFRAGMENT_TAG);
			fragmentTransaction.commitAllowingStateLoss();
		}
		else
		{
			Intent startIntent = new Intent(this, DetailActivity.class).setData(dateUri);
			startActivity(startIntent);
		}

	}
}
