package com.hally.sunshine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.hally.sunshine.R;
import com.hally.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by Kateryna Levshova on 14.03.2016.
 */
public class Util
{
	/**
	 * Returns true if the network is available or about to become available
	 *
	 * @param context - is used to get the ConnectivityManager
	 * @return
	 */
	static public boolean isNetworkAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnectedOrConnecting());
	}

	static public String getPreferredLocation(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_location_key),
				context.getString(R.string.pref_location_default));
	}

	static public boolean isMetric(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_units_key),
				context.getString(R.string.pref_units_metric))
				.equals(context.getString(R.string.pref_units_metric));
	}

	/**
	 * Returns location status
	 * @param context
	 * @return
	 */
	@SuppressWarnings("ResourceType")
	static public @SunshineSyncAdapter.LocationStatus int getLocationStatus(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(context.getString(R.string.pref_location_status_key),
				SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN);
	}

	/**
	 * Sets the location status into shared preference. Should not be called from the UI thread
	 * because it uses commit to write to the shared preferences.
	 *
	 * @param context
	 * @param locationStatus
	 */
	static public void setLocationStatusPref(Context context, int locationStatus)
	{
		SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences
				(context).edit();
		prefsEditor.putInt(context.getResources().getString(R.string.pref_location_status_key),
				locationStatus);
		prefsEditor.commit(); // because the function is used in BG thread, use apply for UI
	}

	/**
	 * Resets th location status to <code>SunshineSyncAdapter.LocationStatus
	 * .LOCATION_STATUS_INVALID</code>
	 * @param context
	 */
	static public void resentLocationUnknown(Context context)
	{
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefs.putInt(context.getResources().getString(R.string.pref_location_status_key),
				SunshineSyncAdapter.LOCATION_STATUS_INVALID);
		prefs.apply(); // it is called from UI thread
	}
}
