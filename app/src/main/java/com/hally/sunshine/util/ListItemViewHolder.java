package com.hally.sunshine.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hally.sunshine.R;

/**
 * @author Kateryna Levshova
 * @date 20.03.2015
 */
public class ListItemViewHolder
{
	public final ImageView iconView;
	public final TextView dateView;
	public final TextView descriptionView;
	public final TextView highTempView;
	public final TextView lowTempView;

	public ListItemViewHolder(View view)
	{
		iconView = (ImageView) view.findViewById(R.id.detail_icon);
		dateView = (TextView) view.findViewById(R.id.detail_date_day_textview);
		descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
		highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
		lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
	}
}
