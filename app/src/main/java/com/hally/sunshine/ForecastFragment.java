package com.hally.sunshine;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	public ForecastFragment()
	{
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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

		ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecastArrayList);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		ListView listViewForecast = (ListView) rootView.findViewById(R.id
				.listview_forecast);
		listViewForecast.setAdapter(forecastAdapter);

		//FetchWeekWeatherTask fetchWeekWeatherTask = new FetchWeekWeatherTask();
		//fetchWeekWeatherTask.execute();
		return rootView;
	}
}
