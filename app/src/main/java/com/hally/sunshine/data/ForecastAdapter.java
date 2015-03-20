package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hally.sunshine.R;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.view.MainForecastFragment;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts from a {@link
 * android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
	public ForecastAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);
	}

	/**
	 * Prepare the weather high/lows for presentation.
	 */
	private String formatHighLows(double high, double low)
	{
		boolean isMetric = FormatUtil.isMetric(mContext);
		String highLowStr = FormatUtil.formatTemperature(high, isMetric) + "/" +
				FormatUtil.formatTemperature(low, isMetric);
		return highLowStr;
	}

	/*
	This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
	string.
	*/
	private String convertCursorRowToUXFormat(Cursor cursor)
	{
		String highAndLow = formatHighLows(
				cursor.getDouble(MainForecastFragment.COL_WEATHER_MAX_TEMP),
				cursor.getDouble(MainForecastFragment.COL_WEATHER_MIN_TEMP));

		return FormatUtil.formatDate(cursor.getLong(MainForecastFragment.COL_WEATHER_DATE)) +
				" - " + cursor.getString(MainForecastFragment.COL_WEATHER_DESC) +
				" - " + highAndLow;
	}

	/*
	Remember that these views are reused as needed.
	*/
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		View view =
				LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
		return view;
	}

	/*
	This is where we fill-in the views with the contents of the cursor.
	*/
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		// our view is pretty simple here --- just a text view
		// we'll keep the UI functional with a simple (and slow!) binding.
		// Read weather icon ID from cursor
		int weatherId = cursor.getInt(MainForecastFragment.COL_WEATHER_ID);
		// Use placeholder image for now
		ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
		iconView.setImageResource(R.mipmap.ic_launcher);

		// Read date from cursor
		long dateInMillis = cursor.getLong(MainForecastFragment.COL_WEATHER_DATE);

		// Find TextView and set formatted date on it
		TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
		dateView.setText(FormatUtil.getFriendlyDayString(context, dateInMillis));

		// Read weather forecast from cursor
		String description = cursor.getString(MainForecastFragment.COL_WEATHER_DESC);

		// Find TextView and set weather forecast on it
		TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
		descriptionView.setText(description);

		// Read user preference for metric or imperial temperature units
		boolean isMetric = FormatUtil.isMetric(context);

		// Read high temperature from cursor
		double high = cursor.getDouble(MainForecastFragment.COL_WEATHER_MAX_TEMP);
		TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
		highView.setText(FormatUtil.formatTemperature(high, isMetric));

		// Read low temperature from cursor
		double low = cursor.getDouble(MainForecastFragment.COL_WEATHER_MIN_TEMP);
		TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
		lowView.setText(FormatUtil.formatTemperature(low, isMetric));
	}
}


