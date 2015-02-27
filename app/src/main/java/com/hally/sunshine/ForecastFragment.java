package com.hally.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 */
public class ForecastFragment extends Fragment
{
	public final String MOUNTAINVIEW_POSTAL_CODE = "94043";
	private ArrayAdapter<String> _forecastAdapter;

	public ArrayAdapter<String> getForecastAdapter()
	{
		return _forecastAdapter;
	}

	public ForecastFragment()
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
		String[] forecastArray = {
				"Mon 6/23 - Sunny - 31/17",
				"Tue 6/24 - Foggy - 21/8",
				"Wed 6/25 - Cloudy - 22/17",
				"Thurs 6/26 - Rainy - 18/11",
				"Fri 6/27 - Foggy - 21/10",
				"Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
				"Sun 6/29 - Sunny - 20/7"
		};

		List<String> weekForecastArrayList = new ArrayList<String>(Arrays.asList(forecastArray));

		_forecastAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item_forecast, R.id.list_item_forecast_textview,
				weekForecastArrayList);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ListView listViewForecast = (ListView) rootView.findViewById(R.id
				.listview_forecast);
		listViewForecast.setAdapter(_forecastAdapter);

		listViewForecast.setOnItemClickListener(onForecastItemClickListener);

		return rootView;
	}

	private AdapterView.OnItemClickListener onForecastItemClickListener = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			String forecastText = _forecastAdapter.getItem(position);
			/*Toast toast = Toast.makeText(getActivity(), forecastText, Toast.LENGTH_SHORT);
			toast.show();*/

			Intent launchDetailActivity = new Intent(getActivity(), DetailActivity.class);
			launchDetailActivity.putExtra(Intent.EXTRA_TEXT, forecastText);
			startActivity(launchDetailActivity);
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_forecastfragment, menu);
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

			FetchWeekWeatherTask fetchWeekWeatherTask = new FetchWeekWeatherTask(_forecastAdapter);
			fetchWeekWeatherTask.execute(MOUNTAINVIEW_POSTAL_CODE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

