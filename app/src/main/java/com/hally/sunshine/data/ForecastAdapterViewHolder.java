package com.hally.sunshine.data;

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
public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder
{
	public final ImageView iconView;
	public final TextView dateView;
	public final TextView descriptionView;
	public final TextView highTempView;
	public final TextView lowTempView;

	public ForecastAdapterViewHolder(View view)
	{
		super(view);
		iconView = (ImageView) view.findViewById(R.id.detail_icon);
		dateView = (TextView) view.findViewById(R.id.detail_date_day_textview);
		descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
		highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
		lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
	}
}
