package com.hally.sunshine.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hally.sunshine.R;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.TraceUtil;

import org.apache.http.protocol.HTTP;

/**
 * @author Kateryna Levshova
 * @date 26.02.2015
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	private final String CLASS_NAME = DetailFragment.class.getSimpleName();
	private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
	private String _forecastStr;
	private ShareActionProvider _shareActionProvider;

	private static final String[] FORECAST_COLUMNS = {
			WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
	};
	// these constants correspond to the projection defined above, and must change if the
	// projection changes
	private static final int COL_WEATHER_ID = 0;
	private static final int COL_WEATHER_DATE = 1;
	private static final int COL_WEATHER_DESC = 2;
	private static final int COL_WEATHER_MAX_TEMP = 3;
	private static final int COL_WEATHER_MIN_TEMP = 4;


	public DetailFragment()
	{
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		Intent intent = getActivity().getIntent();
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		if (intent != null)
		{
			_forecastStr = intent.getDataString();
			((TextView) rootView.findViewById(R.id.detail_text)).setText(_forecastStr);
		}

		return rootView;
	}

	/**
	 * Sets text to <code>detail_text</code>
	 *
	 * @param str
	 */
	private void setForecastString(String str)
	{
		((TextView) getActivity().findViewById(R.id.detail_text))
				.setText(str);
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
		shareIntent.setType(HTTP.PLAIN_TEXT_TYPE);// "text/plain" MIME type
		shareIntent.putExtra(Intent.EXTRA_TEXT, _forecastStr + FORECAST_SHARE_HASHTAG);
		return shareIntent;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_detail_fragment, menu);

		MenuItem itemShare = menu.findItem(R.id.item_share);

		ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(
						itemShare);

		if (shareActionProvider != null)
		{
			shareActionProvider.setShareIntent(createShareForecastIntent());
		}
		else
		{
			TraceUtil.logD(CLASS_NAME, "onCreateOptionsMenu", "Share Action provider is null?");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		TraceUtil.logV(CLASS_NAME, "onCreateLoader", "");
		Intent intent = getActivity().getIntent();
		if (intent == null)
		{
			return null;
		}

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(
				getActivity(),
				intent.getData(),
				FORECAST_COLUMNS,
				null,
				null,
				null
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		TraceUtil.logV(CLASS_NAME, "onLoadFinished", "");

		if (!data.moveToFirst())
		{
			return;
		}

		String dateString = FormatUtil.formatDate(data.getLong(COL_WEATHER_DATE));
		String weatherDescription = data.getString(COL_WEATHER_DESC);
		boolean isMetric = FormatUtil.isMetric(getActivity());
		String high = FormatUtil.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
		String low = FormatUtil.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
		_forecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
		TextView detailTextView = (TextView) getView().findViewById(R.id.detail_text);
		detailTextView.setText(_forecastStr);

		// If onCreateOptionsMenu has already happened, we need to update the share intent now.
		if (_shareActionProvider != null)
		{
			_shareActionProvider.setShareIntent(createShareForecastIntent());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}
}
