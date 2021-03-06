package com.hally.sunshine.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hally.sunshine.R;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.ImageResouceUtil;
import com.hally.sunshine.util.TraceUtil;
import com.hally.sunshine.util.Util;

/**
 * @author Kateryna Levshova
 * @date 26.02.2015
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	public static final String DETAIL_URI = "URI";
	public static final int COL_WEATHER_HUMIDITY = 5;
	public static final int COL_WEATHER_PRESSURE = 6;
	public static final int COL_WEATHER_WIND_SPEED = 7;
	public static final int COL_WEATHER_DEGREES = 8;
	public static final int COL_WEATHER_CONDITION_ID = 9;
	private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
	private static final int DETAIL_LOADER_ID = 0;
	private static final String[] DETAIL_FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
			WeatherContract.WeatherEntry.COLUMN_PRESSURE,
			WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
			WeatherContract.WeatherEntry.COLUMN_DEGREES,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			// This works because the WeatherProvider returns location data joined with
			// weather data, even though they're stored in two different tables.
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING

	};
	// these constants correspond to the projection defined above, and must change if the
	// projection changes
	private static final int COL_WEATHER_ID = 0;
	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESC = 2;
	private static final int COL_WEATHER_MAX_TEMP = 3;
	private static final int COL_WEATHER_MIN_TEMP = 4;
	private final String CLASS_NAME = DetailFragment.class.getSimpleName();
	private Uri _uri;
	private String _forecastStr;
	private ShareActionProvider _shareActionProvider;
	private ImageView _iconView;
	private TextView _dateDayView;
	private TextView _descriptionView;
	private TextView _highTempView;
	private TextView _lowTempView;
	private TextView _humidityView;
	private TextView _windView;
	private TextView _pressureView;
	private TextView _humidityLabelView;
	private TextView _windLabelView;
	private TextView _pressureLabelView;


	public DetailFragment()
	{
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		Bundle args = getArguments();
		if (args != null)
		{
			_uri = args.getParcelable(DetailFragment.DETAIL_URI);
		}

		View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
		_iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
		_dateDayView = (TextView) rootView.findViewById(R.id.detail_date_day_textview);
		_descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
		_highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
		_lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
		_humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
		_windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
		_pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

		_humidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_text_view);
		_windLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
		_pressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);

		clearDetails();

		return rootView;
	}

	/**
	 * Creates share intent with "text/plain" MIME type and forecast extra text
	 *
	 * @return
	 */
	private Intent createShareForecastIntent()
	{
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		//return to your app in backstack
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");// "text/plain" MIME type
		shareIntent.putExtra(Intent.EXTRA_TEXT, _forecastStr + FORECAST_SHARE_HASHTAG);
		return shareIntent;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		if (getActivity() instanceof DetailActivity)
		{
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.menu_detail_fragment, menu);
			finishCreatingMenu(menu);
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		TraceUtil.logV(CLASS_NAME, "onCreateLoader", "");

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		if (_uri != null)
		{
			return new CursorLoader(
					getActivity(),
					_uri,
					DETAIL_FORECAST_COLUMNS,
					null,
					null,
					null
			);
		}

		ViewParent vp = getView().getParent();
		if (vp instanceof CardView)
		{
			((View) vp).setVisibility(View.INVISIBLE);
		}

		return null;
	}

	private void clearDetails()
	{
		_iconView.setImageResource(android.R.color.transparent);
		_dateDayView.setText("");
		_descriptionView.setText("");
		_highTempView.setText("");
		_lowTempView.setText("");
		_humidityView.setText("");
		_windView.setText("");
		_pressureView.setText("");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		TraceUtil.logV(CLASS_NAME, "onLoadFinished", "");

		if (data != null && data.moveToFirst())
		{
			ViewParent vp = getView().getParent();
			if (vp instanceof CardView)
			{
				((View) vp).setVisibility(View.VISIBLE);
			}

			// Read weather condition ID from cursor
			int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

			if (Util.isDefaultArtPack(getActivity()))
			{
				_iconView.setImageResource(
						ImageResouceUtil.getArtResourceForWeatherCondition(weatherId));
			}
			else
			{
				// Use weather art image
				Glide.with(this)
						.load(ImageResouceUtil
								.getArtUrlForWeatherCondition(getContext(), weatherId))
						.error(ImageResouceUtil.getArtResourceForWeatherCondition(weatherId))
						.crossFade()
						.into(_iconView);
			}

			// Read date from cursor and update views for day of week and date
			long date = data.getLong(COL_WEATHER_DATE);
			String dateText = FormatUtil.getFriendlyDayString(getActivity(), date);
			_dateDayView.setText(dateText);

			// Read description from weather condition ID
			String description = FormatUtil.getStringForWeatherCondition(getActivity(), weatherId);
			_descriptionView.setText(description);
			_descriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

			// For accessibility, add a content description to the icon field
			_iconView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

			// Read high temperature from cursor and update view
			double high = data.getDouble(COL_WEATHER_MAX_TEMP);
			String highString = FormatUtil.formatTemperature(getActivity(), high);
			_highTempView.setText(highString);
			_highTempView.setContentDescription(getString(R.string.a11y_high_temp, highString));

			// Read low temperature from cursor and update view
			double low = data.getDouble(COL_WEATHER_MIN_TEMP);
			String lowString = FormatUtil.formatTemperature(getActivity(), low);
			_lowTempView.setText(lowString);
			_lowTempView.setContentDescription(getString(R.string.a11y_low_temp, lowString));

			// Read humidity from cursor and update view
			float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
			_humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
			_humidityLabelView.setContentDescription(
					getString(R.string.a11y_humidity, _humidityLabelView.getText()));
			_humidityLabelView.setContentDescription(_humidityLabelView.getContentDescription());

			// Read wind speed and direction from cursor and update view
			float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
			float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
			_windView.setText(FormatUtil.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
			_windLabelView.setContentDescription(
					getString(R.string.a11y_wind, _windLabelView.getText()));
			_windLabelView.setContentDescription(_windLabelView.getContentDescription());

			// Read pressure from cursor and update view
			float pressure = data.getFloat(COL_WEATHER_PRESSURE);
			_pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
			_pressureLabelView.setContentDescription(
					getString(R.string.a11y_pressure, _pressureLabelView.getText()));
			_pressureLabelView.setContentDescription(_pressureLabelView.getContentDescription());

			// We still need this for the share intent
			_forecastStr = String.format("%s - %s - %s/%s", dateText, description, high, low);
		}
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

		// We need to start the enter transition after the data has loaded
		if (activity instanceof DetailActivity)
		{
			activity.supportStartPostponedEnterTransition();

			if (null != toolbarView)
			{
				activity.setSupportActionBar(toolbarView);

				activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
				activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		else
		{
			if (null != toolbarView)
			{
				Menu menu = toolbarView.getMenu();
				if (null != menu)
				{
					menu.clear();
				}
				toolbarView.inflateMenu(R.menu.menu_detail_fragment);
				finishCreatingMenu(toolbarView.getMenu());
			}
		}
	}

	private void finishCreatingMenu(Menu menu)
	{
		// Retrieve the share menu item
		MenuItem menuItem = menu.findItem(R.id.item_share);
		menuItem.setIntent(createShareForecastIntent());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	public void onLocationChanged(String newLocation)
	{
		Uri uri = _uri;
		// replace the uri, since the location has changed
		if (uri != null)
		{
			long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
			Uri updatedUri =
					WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
			_uri = updatedUri;
			getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
		}
	}


}
