package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.hally.sunshine.R;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.ImageResouceUtil;
import com.hally.sunshine.view.MainForecastFragment;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts from a {@link
 * android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapterViewHolder>
{
	private static final int VIEW_TYPE_COUNT = 2;
	private static final int VIEW_TYPE_TODAY = 0;
	private static final int VIEW_TYPE_FUTURE_DAY = 1;
	final private Context _context;
	private Cursor _cursor;
	private boolean _showTodayItem = true;


	public ForecastAdapter(Context context)
	{
		_context = context;
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

	/*
	   This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
	   RecyclerView that will be used to contain the view, so that it can get the current
	   ItemSelectionManager from the view.
	   One could implement this pattern without modifying RecyclerView by taking advantage
	   of the view tag to store the ItemChoiceManager.
	*/
	@Override
	public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
	{
		if (viewGroup instanceof RecyclerView)
		{
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
			View view =
					LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
			view.setFocusable(true);
			return new ForecastAdapterViewHolder(view);
		}
		else
		{
			throw new RuntimeException("Not bound to RecyclerViewSelection");
		}
	}

	@Override
	public int getItemCount()
	{
		if (null == _cursor)
		{
			return 0;
		}
		return _cursor.getCount();
	}

	public void swapCursor(Cursor newCursor)
	{
		_cursor = newCursor;
		notifyDataSetChanged();
	}

	public Cursor getCursor()
	{
		return _cursor;
	}

	/*
	This is where we fill-in the views with the contents of the cursor.
	*/
	@Override
	public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position)
	{
		{
			_cursor.moveToPosition(position);
			int weatherId = _cursor.getInt(MainForecastFragment.COL_WEATHER_CONDITION_ID);
			int defaultIcon;

			switch (getItemViewType(_cursor.getPosition()))
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

			Glide.with(_context).load(ImageResouceUtil.getArtUrlForWeatherCondition(_context,
					weatherId)).error(defaultIcon).crossFade()
					.into(forecastAdapterViewHolder.iconView);

			// Read date from cursor
			long dateInMillis = _cursor.getLong(MainForecastFragment.COL_WEATHER_DATE);

			// Find TextView and set formatted date on it
			forecastAdapterViewHolder.dateView.setText(FormatUtil.getFriendlyDayString(_context,
					dateInMillis));

			// Get description from weather condition ID
			String description = FormatUtil.getStringForWeatherCondition(_context, weatherId);

			// Find TextView and set weather forecast on it
			forecastAdapterViewHolder.descriptionView.setText(description);

			// null is appropriate when the image is purely decorative or when the image already
			// has text describing it in the same UI component
			forecastAdapterViewHolder.iconView.setContentDescription(null);

			// Read high temperature from cursor
			double high = _cursor.getDouble(MainForecastFragment.COL_WEATHER_MAX_TEMP);

			String highString = FormatUtil.formatTemperature(_context, high);
			forecastAdapterViewHolder.highTempView.setText(highString);
			forecastAdapterViewHolder.highTempView
					.setContentDescription(_context.getResources().getString(R
							.string.a11y_high_temp, highString));

			// Read low temperature from cursor
			double low = _cursor.getDouble(MainForecastFragment.COL_WEATHER_MIN_TEMP);
			String lowString = FormatUtil.formatTemperature(_context, low);
			forecastAdapterViewHolder.lowTempView.setText(lowString);
			forecastAdapterViewHolder.lowTempView.setContentDescription(
					_context.getResources().getString(R.string.a11y_low_temp, lowString));
		}
	}
}


