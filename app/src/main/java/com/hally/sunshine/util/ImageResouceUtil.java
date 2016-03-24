package com.hally.sunshine.util;

import android.content.Context;

import com.hally.sunshine.R;

/**
 * @author Kateryna Levshova
 * @date 24.03.2015
 */
public class ImageResouceUtil
{
	/**
	 * Helper method to provide the icon resource id according to the weather condition id returned
	 * by the OpenWeatherMap call.
	 *
	 * @param weatherId from OpenWeatherMap API response
	 * @return resource id for the corresponding icon. -1 if no relation is found.
	 */
	public static int getIconResourceForWeatherCondition(int weatherId)
	{
// Based on weather code data found at:
// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		if (weatherId >= 200 && weatherId <= 232)
		{
			return R.mipmap.ic_storm;
		}
		else if (weatherId >= 300 && weatherId <= 321)
		{
			return R.mipmap.ic_light_rain;
		}
		else if (weatherId >= 500 && weatherId <= 504)
		{
			return R.mipmap.ic_rain;
		}
		else if (weatherId == 511)
		{
			return R.mipmap.ic_snow;
		}
		else if (weatherId >= 520 && weatherId <= 531)
		{
			return R.mipmap.ic_rain;
		}
		else if (weatherId >= 600 && weatherId <= 622)
		{
			return R.mipmap.ic_snow;
		}
		else if (weatherId >= 701 && weatherId <= 761)
		{
			return R.mipmap.ic_fog;
		}
		else if (weatherId == 761 || weatherId == 781)
		{
			return R.mipmap.ic_storm;
		}
		else if (weatherId == 800)
		{
			return R.mipmap.ic_clear;
		}
		else if (weatherId == 801)
		{
			return R.mipmap.ic_light_clouds;
		}
		else if (weatherId >= 802 && weatherId <= 804)
		{
			return R.mipmap.ic_cloudy;
		}
		return -1;
	}

	/**
	 * Helper method to provide the art resource id according to the weather condition id returned
	 * by the OpenWeatherMap call.
	 *
	 * @param weatherId from OpenWeatherMap API response
	 * @return resource id for the corresponding icon. -1 if no relation is found.
	 */
	public static int getArtResourceForWeatherCondition(int weatherId)
	{
// Based on weather code data found at:
// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		if (weatherId >= 200 && weatherId <= 232)
		{
			return R.mipmap.art_storm;
		}
		else if (weatherId >= 300 && weatherId <= 321)
		{
			return R.mipmap.art_light_rain;
		}
		else if (weatherId >= 500 && weatherId <= 504)
		{
			return R.mipmap.art_rain;
		}
		else if (weatherId == 511)
		{
			return R.mipmap.art_snow;
		}
		else if (weatherId >= 520 && weatherId <= 531)
		{
			return R.mipmap.art_rain;
		}
		else if (weatherId >= 600 && weatherId <= 622)
		{
			return R.mipmap.art_snow;
		}
		else if (weatherId >= 701 && weatherId <= 761)
		{
			return R.mipmap.art_fog;
		}
		else if (weatherId == 761 || weatherId == 781)
		{
			return R.mipmap.art_storm;
		}
		else if (weatherId == 800)
		{
			return R.mipmap.art_clear;
		}
		else if (weatherId == 801)
		{
			return R.mipmap.art_light_clouds;
		}
		else if (weatherId >= 802 && weatherId <= 804)
		{
			return R.mipmap.art_clouds;
		}
		return -1;
	}

	/**
	 * Helper method to provide the art urls according to the weather condition id returned by the
	 * OpenWeatherMap call.
	 *
	 * @param context   Context to use for retrieving the URL format
	 * @param weatherId from OpenWeatherMap API response
	 * @return url for the corresponding weather artwork. null if no relation is found.
	 */
	public static String getArtUrlForWeatherCondition(Context context, int weatherId)
	{
		// Based on weather code data found at:
		// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		if (weatherId >= 200 && weatherId <= 232)
		{
			return context.getString(R.string.format_art_url, "storm");
		}
		else if (weatherId >= 300 && weatherId <= 321)
		{
			return context.getString(R.string.format_art_url, "light_rain");
		}
		else if (weatherId >= 500 && weatherId <= 504)
		{
			return context.getString(R.string.format_art_url, "rain");
		}
		else if (weatherId == 511)
		{
			return context.getString(R.string.format_art_url, "snow");
		}
		else if (weatherId >= 520 && weatherId <= 531)
		{
			return context.getString(R.string.format_art_url, "rain");
		}
		else if (weatherId >= 600 && weatherId <= 622)
		{
			return context.getString(R.string.format_art_url, "snow");
		}
		else if (weatherId >= 701 && weatherId <= 761)
		{
			return context.getString(R.string.format_art_url, "fog");
		}
		else if (weatherId == 761 || weatherId == 781)
		{
			return context.getString(R.string.format_art_url, "storm");
		}
		else if (weatherId == 800)
		{
			return context.getString(R.string.format_art_url, "clear");
		}
		else if (weatherId == 801)
		{
			return context.getString(R.string.format_art_url, "light_clouds");
		}
		else if (weatherId >= 802 && weatherId <= 804)
		{
			return context.getString(R.string.format_art_url, "clouds");
		}
		return null;
	}
}
