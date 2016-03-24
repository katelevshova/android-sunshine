package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
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
		int defaultIcon;

		switch (viewType)
		{
			case VIEW_TYPE_TODAY:
			{
				defaultIcon = ImageResouceUtil.getArtResourceForWeatherCondition(weatherId);
				break;
			}
			default:
			{
				defaultIcon = ImageResouceUtil.getIconResourceForWeatherCondition(weatherId);
			}
		}

		Glide.with(context).load(ImageResouceUtil.getArtUrlForWeatherCondition(context,
				weatherId)).error(defaultIcon).crossFade().into(listItemViewHolder.iconView);

		// Read date from cursor
		long dateInMillis = cursor.getLong(MainForecastFragment.COL_WEATHER_DATE);

		// Find TextView and set formatted date on it
		listItemViewHolder.dateView.setText(FormatUtil.getFriendlyDayString(context, dateInMillis));

		// Get description from weather condition ID
		String description = FormatUtil.getStringForWeatherCondition(context, weatherId);

		// Find TextView and set weather forecast on it
		listItemViewHolder.descriptionView.setText(description);

		// null is appropriate when the image is purely decorative or when the image already
		// has text describing it in the same UI component
		listItemViewHolder.iconView.setContentDescription(null);

		// Read high temperature from cursor
		double high = cursor.getDouble(MainForecastFragment.COL_WEATHER_MAX_TEMP);

		String highString = FormatUtil.formatTemperature(context, high);
		listItemViewHolder.highTempView.setText(highString);
		listItemViewHolder.highTempView.setContentDescription(context.getResources().getString(R
				.string.a11y_high_temp,	highString));

		// Read low temperature from cursor
		double low = cursor.getDouble(MainForecastFragment.COL_WEATHER_MIN_TEMP);
		String lowString = FormatUtil.formatTemperature(context, low);
		listItemViewHolder.lowTempView.setText(lowString);
		listItemViewHolder.lowTempView.setContentDescription(context.getResources().getString(R.string.a11y_low_temp, lowString));
	}
}


