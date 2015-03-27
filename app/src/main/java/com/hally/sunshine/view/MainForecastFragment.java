package com.hally.sunshine.view;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.hally.sunshine.FetchWeatherTask;
import com.hally.sunshine.R;
import com.hally.sunshine.data.ForecastAdapter;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.model.IForecastFragmentCallback;
import com.hally.sunshine.util.FormatUtil;

;


/**
 *
 */
public class MainForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	private ForecastAdapter _forecastAdapter;
	public static final int FORECAST_LOADER_ID = 0;

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

	/**
	 * Returns forecast adapter
	 *
	 * @return _forecastAdapter
	 */
	public ForecastAdapter getForecastAdapter()
	{
		return _forecastAdapter;
	}

	public MainForecastFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		_forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ListView listViewForecast = (ListView) rootView.findViewById(R.id
				.listview_forecast);
		listViewForecast.setAdapter(_forecastAdapter);

		listViewForecast.setOnItemClickListener(onForecastItemClickListener);

		return rootView;
	}

	private AdapterView.OnItemClickListener onForecastItemClickListener =
	new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
								long id)
		{
			// CursorAdapter returns a cursor at the correct position for getItem(), or null
			// if it cannot seek to that position.
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

			if (cursor != null)
			{
				String locationSetting = FormatUtil.getPreferredLocation(getActivity());
				((IForecastFragmentCallback) getActivity()).onItemSelected(
						WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
								locationSetting, cursor.getLong(COL_WEATHER_DATE)));
			}
		}
	};

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

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Updates weather information in the ListView using <code>FetchWeatherTask</> class
	 */
	private void updateWeather()
	{
		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(), _forecastAdapter);
		String location = FormatUtil.getPreferredLocation(getActivity());
		fetchWeatherTask.execute(location);
	}

	// since we read the location when we create the loader, all we need to do is restart things
	void onLocationChanged()
	{
		updateWeather();
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
		String locationSetting = FormatUtil.getPreferredLocation(getActivity());
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
	}

	@Override
	public void onLoaderReset(Loader loader)
	{
		_forecastAdapter.swapCursor(null); // release any resources which we might be using
	}
}

