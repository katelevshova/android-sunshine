package com.hally.sunshine.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hally.sunshine.R;
import com.hally.sunshine.data.ForecastAdapter;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.model.IForecastFragmentCallback;
import com.hally.sunshine.sync.SunshineSyncAdapter;
import com.hally.sunshine.util.TraceUtil;
import com.hally.sunshine.util.Util;

;


/**
 *
 */
public class MainForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	public static final int FORECAST_LOADER_ID = 0;
	// These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these
	// must change.
	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_WEATHER_DESC = 2;
	public static final int COL_WEATHER_MAX_TEMP = 3;
	public static final int COL_WEATHER_MIN_TEMP = 4;
	public static final int COL_LOCATION_SETTING = 5;
	public static final int COL_WEATHER_CONDITION_ID = 6;
	public static final int COL_COORD_LAT = 7;
	public static final int COL_COORD_LONG = 8;
	public static final String SELECTED_KEY = "selectedKey";
	// For the forecast view we're showing only a small subset of the stored data.
	// Specify the columns we need.
	private static final String[] FORECAST_COLUMNS = {
			// In this case the id needs to be fully qualified with a table name, since
			// the content provider joins the location & weather tables in the background
			// (both have an _id column)
			// On the one hand, that's annoying. On the other, you can search the weather table
			// using the location set by the user, which is only in the Location table.
			// So the convenience is worth it.
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.LocationEntry.COLUMN_COORD_LAT,
			WeatherContract.LocationEntry.COLUMN_COORD_LONG
	};
	private final String CLASS_NAME = MainForecastFragment.class.getSimpleName();
	private ForecastAdapter _forecastAdapter;
	private int _selectedPosition = 0;
	private ListView _listViewForecast;
	private AdapterView.OnItemClickListener _onForecastItemClickListener =
			new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position,
										long id)
				{
					// CursorAdapter returns a cursor at the correct position for getItem(), or null
					// if it cannot seek to that position.
					Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

					showForecastDetails(cursor);
					_selectedPosition = position;
				}
			};

	private SharedPreferences.OnSharedPreferenceChangeListener _onSharedPreferenceChangeListener =
			new SharedPreferences.OnSharedPreferenceChangeListener()
			{
				@Override
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
				{
					if(key.equals(getString(R.string.pref_location_status_key)))
					{
						updateEmptyView();
					}
				}
			};

	public MainForecastFragment()
	{
		// Required empty public constructor
	}

	/**
	 * Returns forecast adapter
	 *
	 * @return _forecastAdapter
	 */
	public ForecastAdapter getForecastAdapter()
	{
		return _forecastAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		pref.registerOnSharedPreferenceChangeListener(_onSharedPreferenceChangeListener);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		pref.unregisterOnSharedPreferenceChangeListener(_onSharedPreferenceChangeListener);
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		_forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		_listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
		View emptyView = rootView.findViewById(R.id.listview_no_forecast);
		_listViewForecast.setEmptyView(emptyView);
		_listViewForecast.setAdapter(_forecastAdapter);

		_listViewForecast.setOnItemClickListener(_onForecastItemClickListener);

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
		{
			_selectedPosition = savedInstanceState.getInt(SELECTED_KEY);
		}

		return rootView;
	}

	public void setUseTodayItem(boolean flag)
	{
		if (_forecastAdapter != null)
		{
			_forecastAdapter.setIsTodayItemNecessary(flag);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		if (_selectedPosition != ListView.INVALID_POSITION)
		{
			outState.putInt(SELECTED_KEY, _selectedPosition);
		}
		super.onSaveInstanceState(outState);
	}

	private void showForecastDetails(Cursor cursor)
	{
		if (cursor != null)
		{
			String locationSetting = Util.getPreferredLocation(getActivity());
			((IForecastFragmentCallback) getActivity()).onItemSelected(
					WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
							locationSetting, cursor.getLong(COL_WEATHER_DATE)));
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_main_f_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.item_refresh)
		{
			updateWeather();
			return true;
		}

		if (id == R.id.item_map)
		{
			openPreferredLocationMap();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Updates weather information in the ListView using <code>AlarmService</> class
	 */
	private void updateWeather()
	{
		SunshineSyncAdapter.syncImmediately(getActivity());
	}

	// since we read the location when we create the loader, all we need to do is restart things
	void onLocationChanged()
	{
		getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
	{
		String locationSetting = Util.getPreferredLocation(getActivity());
		// Sort order: Ascending, by date.
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
				locationSetting, System.currentTimeMillis());
		return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null,
				sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		_forecastAdapter.swapCursor(cursor);

		if (_selectedPosition != ListView.INVALID_POSITION)
		{
			_listViewForecast.smoothScrollToPosition(_selectedPosition);
		}
		updateEmptyView();
	}

	/**
	 * Updates the empty list view with contextually relevant information that the user can use to
	 * determine why they aren't seeing weather.
	 */
	private void updateEmptyView()
	{
		if (_forecastAdapter.getCount() == 0)
		{
			TextView textView = (TextView) getView().findViewById(R.id.listview_no_forecast);

			//if cursor is empty
			if (textView != null)
			{
				int message = R.string.empty_forecast_list;

				@SunshineSyncAdapter.LocationStatus int locationStatus = Util
						.getLocationStatus(getContext());

				switch(locationStatus)
				{
					case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
					{
						message = R.string.empty_forecast_list_server_down;
						break;
					}
					case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
					case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
					{
						message = R.string.empty_forecast_list_server_error;
						break;
					}
					default:
						if (!Util.isNetworkAvailable(getContext()))
						{
							message = R.string.empty_forecast_list_no_network;
						}
				}

				textView.setText(message);
			}
		}
	}


	@Override
	public void onLoaderReset(Loader loader)
	{
		_forecastAdapter.swapCursor(null); // release any resources which we might be using
	}

	private void openPreferredLocationMap()
	{

		// Using the URI scheme for showing a location found on a map. This super-handy
		// intent can is detailed in the "Common Intents" page of Android's developer site:
		// http://developer.android.com/guide/components/intents-common.html#Maps

		if (_forecastAdapter != null)
		{
			Cursor cursor = _forecastAdapter.getCursor();

			if (cursor != null)
			{
				cursor.moveToPosition(0);

				String posLat = cursor.getString(COL_COORD_LAT);
				String posLong = cursor.getString(COL_COORD_LONG);

				Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(geoLocation);

				if (intent.resolveActivity(getActivity().getPackageManager()) != null)
				{
					startActivity(intent);
				}
				else
				{
					TraceUtil.logD(CLASS_NAME, "openPreferredLocationMap",
							"Couldn't call " + geoLocation.toString() +
									", no receiving apps installed!");
				}
			}
		}
	}
}

