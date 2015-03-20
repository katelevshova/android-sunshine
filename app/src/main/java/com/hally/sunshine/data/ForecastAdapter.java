package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		TextView tv = (TextView) view;
		tv.setText(convertCursorRowToUXFormat(cursor));
	}
}

