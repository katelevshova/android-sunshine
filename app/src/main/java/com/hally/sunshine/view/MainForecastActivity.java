package com.hally.sunshine.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.hally.sunshine.R;
import com.hally.sunshine.gcm.MyRegistrationIntentService;
import com.hally.sunshine.data.IForecastFragmentCallback;
import com.hally.sunshine.sync.SunshineSyncAdapter;
import com.hally.sunshine.util.TraceUtil;
import com.hally.sunshine.util.Util;


public class MainForecastActivity extends ActionBarActivity implements IForecastFragmentCallback
{
	public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
	private static final String DETAILFRAGMENT_TAG = "DFTAG";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final String CLASS_NAME = MainForecastActivity.class.getSimpleName();
	private String _location;
	private boolean _hasTwoPane = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_location = Util.getPreferredLocation(this);

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//getSupportActionBar().setIcon(R.mipmap.art_clear);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

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

		setUseTodayItemElement();

		SunshineSyncAdapter.initializeSyncAdapter(this);

		// If Google Play Services is up to date, we'll want to register GCM. If it is not, we'll
		// skip the registration and this device will not receive any downstream messages from
		// our fake server. Because weather alerts are not a core feature of the app, this should
		// not affect the behavior of the app, from a user perspective.
		if(checkPlayServices())
		{
			// This is where we could either prompt a user that they should install
			// the latest version of Google Play Services, or add an error snackbar
			// that some features won't be available.
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
					(this);
			boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);

			if(!sentToken)
			{
				Intent intent = new Intent(this, MyRegistrationIntentService.class);
				startService(intent);
			}
		}
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

		boolean isDebuggable =
				(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
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

		if (id == R.id.item_db_manager)
		{
			startActivity(new Intent(this, AndroidDatabaseManager.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a
	 * dialog that allows users to download the APK from the Google Play Store or enable it in the
	 * device's system settings.
	 */
	private boolean checkPlayServices()
	{
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (apiAvailability.isUserResolvableError(resultCode))
			{
				apiAvailability.getErrorDialog(this, resultCode,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else
			{
				TraceUtil.logI(CLASS_NAME, "checkPlayServices", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
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
