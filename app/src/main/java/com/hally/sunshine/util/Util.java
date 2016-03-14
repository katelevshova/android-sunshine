package com.hally.sunshine.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kateryna Levshova on 14.03.2016.
 */
public class Util
{
	/**
	 * Returns true if the network is available or about to become available
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
}
