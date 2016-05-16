package com.hally.sunshine.data;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hally.sunshine.R;

/**
 * Cache of the children views for a forecast list item.
 * @author Kateryna Levshova
 * @date 20.03.2015
 */
public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
	public final ImageView iconView;
	public final TextView dateView;
	public final TextView descriptionView;
	public final TextView highTempView;
	public final TextView lowTempView;
	private Cursor _cursor;
	private IForecastAdapterOnClick _clickListener;

	public ForecastAdapterViewHolder(View view, Cursor cursor, IForecastAdapterOnClick
			clickListener)
	{
		super(view);
		_cursor = cursor;
		_clickListener = clickListener;
		iconView = (ImageView) view.findViewById(R.id.detail_icon);
		dateView = (TextView) view.findViewById(R.id.detail_date_day_textview);
		descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
		highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
		lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
		view.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		int position = getAdapterPosition();
		_cursor.moveToPosition(position);
		int dateColumnIndex = _cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
		_clickListener.onClick(_cursor.getLong(dateColumnIndex), this);

	}
}
