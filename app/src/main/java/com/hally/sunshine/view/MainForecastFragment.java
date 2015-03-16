package com.hally.sunshine.view;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hally.sunshine.FetchWeatherTask;
import com.hally.sunshine.R;
import com.hally.sunshine.data.ForecastAdapter;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.util.FormatUtil;

;


/**
 *
 */
public class MainForecastFragment extends Fragment
{
	private ForecastAdapter _forecastAdapter;

	/**
	 * Returns forecast adapter
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
		String locationSetting = FormatUtil.getPreferredLocation(getActivity());
		// Sort order: Ascending, by date.
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
				locationSetting, System.currentTimeMillis());
		Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri,
				null, null, null, sortOrder);
		// The CursorAdapter will take data from our cursor and populate the ListView
		// However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
		// up with an empty list the first time we run.
		_forecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ListView listViewForecast = (ListView) rootView.findViewById(R.id
				.listview_forecast);
		listViewForecast.setAdapter(_forecastAdapter);

		//listViewForecast.setOnItemClickListener(onForecastItemClickListener);

		return rootView;
	}

	/**private AdapterView.OnItemClickListener onForecastItemClickListener =
			new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					String forecastText = _forecastAdapter.getItem(position);
			/*Toast toast = Toast.makeText(getActivity(), forecastText, Toast.LENGTH_SHORT);
			toast.show();*/

				/**	Intent launchDetailActivity = new Intent(getActivity(), DetailActivity.class);
					launchDetailActivity.putExtra(Intent.EXTRA_TEXT, forecastText);
					startActivity(launchDetailActivity);
				}
			};*/

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
		fetchWeatherTask.execute(getLocation());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		updateWeather();
	}

	/**
	 * Checks shared preferences. Returns Location defined in settings_pref_general.xml.
	 *
	 * @return Location String
	 */
	private String getLocation()
	{
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(getActivity());

		String key = getString(R.string.pref_location_key);
		String defaultLocation = getString(R.string.pref_location_default);

		return preferences.getString(key, defaultLocation);
	}
}

