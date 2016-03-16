package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hally.sunshine.R;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.ImageResouceUtil;
import com.hally.sunshine.util.ListItemViewHolder;
import com.hally.sunshine.util.Util;
import com.hally.sunshine.view.MainForecastFragment;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts from a {@link
 * android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
	private static final int VIEW_TYPE_COUNT = 2;
	private static final int VIEW_TYPE_TODAY = 0;
	private static final int VIEW_TYPE_FUTURE_DAY = 1;
	private boolean _showTodayItem = true;

	public ForecastAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);
	}

	public void setIsTodayItemNecessary(boolean flag)
	{
		_showTodayItem = flag;
	}

	@Override
	public int getItemViewType(int position)
	{
		return (position == VIEW_TYPE_TODAY && _showTodayItem) ? VIEW_TYPE_TODAY :
			VIEW_TYPE_FUTURE_DAY;
	}

	@Override
	public int getViewTypeCount()
	{
		return VIEW_TYPE_COUNT;
	}

	/*
	Remember that these views are reused as needed.
	*/
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		int viewType = getItemViewType(cursor.getPosition());
		int layoutId = -1;

		switch (viewType)
		{
			case VIEW_TYPE_TODAY:
			{
				layoutId = R.layout.list_item_forecast_today;
				break;
			}
			case VIEW_TYPE_FUTURE_DAY:
			{
				layoutId = R.layout.list_item_forecast;
				break;
			}
		}

		View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
		ListItemViewHolder listItemViewHolder = new ListItemViewHolder(view);
		view.setTag(listItemViewHolder);

		return view;
	}

	/*
	This is where we fill-in the views with the contents of the cursor.
	*/
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ListItemViewHolder listItemViewHolder = (ListItemViewHolder) view.getTag();
		int weatherId = cursor.getInt(MainForecastFragment.COL_WEATHER_CONDITION_ID);

		int viewType = getItemViewType(cursor.getPosition());

		switch (viewType)
		{
			case VIEW_TYPE_TODAY:
				listItemViewHolder.iconView.setImageResource(
						ImageResouceUtil.getArtResourceForWeatherCondition(weatherId));
				break;
			case VIEW_TYPE_FUTURE_DAY:
				listItemViewHolder.iconView
						.setImageResource(ImageResouceUtil.getIconResourceForWeatherCondition(
								weatherId));
				break;
		}

		// Read date from cursor
		long dateInMillis = cursor.getLong(MainForecastFragment.COL_WEATHER_DATE);

		// Find TextView and set formatted date on it
		listItemViewHolder.dateView.setText(FormatUtil.getFriendlyDayString(context, dateInMillis));

		// Read weather forecast from cursor
		String description = cursor.getString(MainForecastFragment.COL_WEATHER_DESC);

		// Find TextView and set weather forecast on it
		listItemViewHolder.descriptionView.setText(description);

		// For accessibility, add a content description to the icon field
		listItemViewHolder.iconView.setContentDescription(description);

		// Read user preference for metric or imperial temperature units
		boolean isMetric = Util.isMetric(context);

		// Read high temperature from cursor
		double high = cursor.getDouble(MainForecastFragment.COL_WEATHER_MAX_TEMP);
		listItemViewHolder.highTempView
				.setText(FormatUtil.formatTemperature(context, high));

		// Read low temperature from cursor
		double low = cursor.getDouble(MainForecastFragment.COL_WEATHER_MIN_TEMP);
		listItemViewHolder.lowTempView
				.setText(FormatUtil.formatTemperature(context, low));
	}
}


