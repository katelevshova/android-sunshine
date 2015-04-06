package com.hally.sunshine.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hally.sunshine.service.SunshineService;
import com.hally.sunshine.sync.SunshineSyncAdapter;
import com.hally.sunshine.util.FormatUtil;

/**
 * @author Kateryna Levshova
 * @date 06.04.2015
 */
public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent locationQueryIntent = new Intent(context, SunshineService.class);
		locationQueryIntent.putExtra(SunshineSyncAdapter.LOCATION_QUERY_EXTRA, FormatUtil.getPreferredLocation
				(context));
		context.startService(locationQueryIntent);

	}
}
