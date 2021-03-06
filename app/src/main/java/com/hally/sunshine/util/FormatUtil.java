package com.hally.sunshine.util;


import android.content.Context;
import android.text.format.Time;

import com.hally.sunshine.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
public class FormatUtil
{
	private static final int WEEK = 7;

	public static String formatTemperature(Context context, double temperature)
	{
		// Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
		// the values here.
		if (!Util.isMetric(context))
		{
			temperature = (temperature * 1.8) + 32;
		}
		// For presentation, assume the user doesn't care about tenths of a degree.
		return String.format(context.getString(R.string.format_temperature), temperature);
	}

	public static String formatDate(long dateInMillis)
	{
		Date date = new Date(dateInMillis);
		return DateFormat.getDateInstance().format(date);
	}

	/**
	 * Helper method to convert the database representation of the date into something to display to
	 * users. As classy and polished a user experience as "20140102" is, we can do better.
	 *
	 * @param context      Context to use for resource localization
	 * @param dateInMillis The date in milliseconds
	 * @return a user-friendly representation of the date.
	 */
	public static String getFriendlyDayString(Context context, long dateInMillis)
	{
		// The day string for forecast uses the following logic:
		// For today: "Today, June 8"
		// For tomorrow: "Tomorrow"
		// For the next 5 days: "Wednesday" (just the day name)
		// For all days after that: "Mon Jun 8"

		Time time = new Time();
		time.setToNow();
		long currentTime = System.currentTimeMillis();
		int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
		int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);
		String formattedMonthDay = getFormattedMonthDay(context, dateInMillis);

		// If the date we're building the String for is today's date, the format
		// is "Today, June 24"
		if (julianDay == currentJulianDay)
		{
			return getFirstWeekDateFormat(context, context.getString(R.string.today),
					formattedMonthDay);
		}
//		else if (julianDay < currentJulianDay + WEEK)
//		{
//			// If the input date is less than a week in the future, Wednesday, April 09.
//			String dayName = getDayName(context, dateInMillis);
//			return getFirstWeekDateFormat(context, dayName, formattedMonthDay);
//		}
		else
		{
			String currLocale = Locale.getDefault().getLanguage();
			SimpleDateFormat shortenedDateFormat;
			String dayString;

			if(currLocale.equals("ru"))
			{
				shortenedDateFormat = new SimpleDateFormat("EEE, dd MMMM");
				dayString = shortenedDateFormat.format(dateInMillis).toUpperCase();
			}
			else
			{
				// Otherwise, use the form "Mon, Jun 3"
				shortenedDateFormat = new SimpleDateFormat("EEE, MMMM dd");
				dayString = shortenedDateFormat.format(dateInMillis);
			}

			return dayString;
		}
	}

	private static String getFirstWeekDateFormat(Context context, String dayName,
												 String formattedMonthDay)
	{
		return String.format(context.getString(R.string.format_full_friendly_date), dayName,
				formattedMonthDay);
	}

	public static String getFormattedWind(Context context, float windSpeed, float degrees)
	{
		int windFormat;
		if (Util.isMetric(context))
		{
			windFormat = R.string.format_wind_kmh;
		}
		else
		{
			windFormat = R.string.format_wind_mph;
			windSpeed = .621371192237334f * windSpeed;
		}

		String direction = context.getResources().getString(R.string.wind_unknown);

		if (degrees >= 337.5 || degrees < 22.5)
		{
			direction = context.getResources().getString(R.string.wind_N);
		}
		else if (degrees >= 22.5 && degrees < 67.5)
		{
			direction = context.getResources().getString(R.string.wind_NE);
		}
		else if (degrees >= 67.5 && degrees < 112.5)
		{
			direction = context.getResources().getString(R.string.wind_E);
		}
		else if (degrees >= 112.5 && degrees < 157.5)
		{
			direction = context.getResources().getString(R.string.wind_SE);
		}
		else if (degrees >= 157.5 && degrees < 202.5)
		{
			direction = context.getResources().getString(R.string.wind_S);
		}
		else if (degrees >= 202.5 && degrees < 247.5)
		{
			direction = context.getResources().getString(R.string.wind_SW);
		}
		else if (degrees >= 247.5 && degrees < 292.5)
		{
			direction = context.getResources().getString(R.string.wind_W);
		}
		else if (degrees >= 292.5 || degrees < 22.5)
		{
			direction = context.getResources().getString(R.string.wind_NW);
		}
		return String.format(context.getString(windFormat), windSpeed, direction);
	}

	/**
	 * Helper method to provide the string according to the weather condition id returned by the
	 * OpenWeatherMap call.
	 *
	 * @param context   Android context
	 * @param weatherId from OpenWeatherMap API response
	 * @return string for the weather condition. null if no relation is found.
	 */
	public static String getStringForWeatherCondition(Context context, int weatherId)
	{
		// Based on weather code data found at:
		// http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
		int stringId;
		if (weatherId >= 200 && weatherId <= 232)
		{
			stringId = R.string.condition_2xx;
		}
		else if (weatherId >= 300 && weatherId <= 321)
		{
			stringId = R.string.condition_3xx;
		}
		else
		{
			switch (weatherId)
			{
				case 500:
					stringId = R.string.condition_500;
					break;
				case 501:
					stringId = R.string.condition_501;
					break;
				case 502:
					stringId = R.string.condition_502;
					break;
				case 503:
					stringId = R.string.condition_503;
					break;
				case 504:
					stringId = R.string.condition_504;
					break;
				case 511:
					stringId = R.string.condition_511;
					break;
				case 520:
					stringId = R.string.condition_520;
					break;
				case 531:
					stringId = R.string.condition_531;
					break;
				case 600:
					stringId = R.string.condition_600;
					break;
				case 601:
					stringId = R.string.condition_601;
					break;
				case 602:
					stringId = R.string.condition_602;
					break;
				case 611:
					stringId = R.string.condition_611;
					break;
				case 612:
					stringId = R.string.condition_612;
					break;
				case 615:
					stringId = R.string.condition_615;
					break;
				case 616:
					stringId = R.string.condition_616;
					break;
				case 620:
					stringId = R.string.condition_620;
					break;
				case 621:
					stringId = R.string.condition_621;
					break;
				case 622:
					stringId = R.string.condition_622;
					break;
				case 701:
					stringId = R.string.condition_701;
					break;
				case 711:
					stringId = R.string.condition_711;
					break;
				case 721:
					stringId = R.string.condition_721;
					break;
				case 731:
					stringId = R.string.condition_731;
					break;
				case 741:
					stringId = R.string.condition_741;
					break;
				case 751:
					stringId = R.string.condition_751;
					break;
				case 761:
					stringId = R.string.condition_761;
					break;
				case 762:
					stringId = R.string.condition_762;
					break;
				case 771:
					stringId = R.string.condition_771;
					break;
				case 781:
					stringId = R.string.condition_781;
					break;
				case 800:
					stringId = R.string.condition_800;
					break;
				case 801:
					stringId = R.string.condition_801;
					break;
				case 802:
					stringId = R.string.condition_802;
					break;
				case 803:
					stringId = R.string.condition_803;
					break;
				case 804:
					stringId = R.string.condition_804;
					break;
				case 900:
					stringId = R.string.condition_900;
					break;
				case 901:
					stringId = R.string.condition_901;
					break;
				case 902:
					stringId = R.string.condition_902;
					break;
				case 903:
					stringId = R.string.condition_903;
					break;
				case 904:
					stringId = R.string.condition_904;
					break;
				case 905:
					stringId = R.string.condition_905;
					break;
				case 906:
					stringId = R.string.condition_906;
					break;
				case 951:
					stringId = R.string.condition_951;
					break;
				case 952:
					stringId = R.string.condition_952;
					break;
				case 953:
					stringId = R.string.condition_953;
					break;
				case 954:
					stringId = R.string.condition_954;
					break;
				case 955:
					stringId = R.string.condition_955;
					break;
				case 956:
					stringId = R.string.condition_956;
					break;
				case 957:
					stringId = R.string.condition_957;
					break;
				case 958:
					stringId = R.string.condition_958;
					break;
				case 959:
					stringId = R.string.condition_959;
					break;
				case 960:
					stringId = R.string.condition_960;
					break;
				case 961:
					stringId = R.string.condition_961;
					break;
				case 962:
					stringId = R.string.condition_962;
					break;
				default:
					return context.getString(R.string.condition_unknown, weatherId);
			}
		}
		return context.getString(stringId);
	}

	/**
	 * Given a day, returns just the name to use for that day. E.g "today", "tomorrow",
	 * "wednesday".
	 *
	 * @param context      Context to use for resource localization
	 * @param dateInMillis The date in milliseconds
	 * @return
	 */
	public static String getDayName(Context context, long dateInMillis)
	{
		// If the date is today, return the localized version of "Today" instead of the actual
		// day name.

		Time t = new Time();
		t.setToNow();
		int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
		int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
		if (julianDay == currentJulianDay)
		{
			return context.getString(R.string.today);
		}
		else if (julianDay == currentJulianDay + 1)
		{
			return context.getString(R.string.tomorrow);
		}
		else
		{
			Time time = new Time();
			time.setToNow();
			// Otherwise, the format is just the day of the week (e.g "Wednesday".

			String currLocale = Locale.getDefault().getLanguage();
			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");;
			String dayString;

			if(currLocale.equals("ru"))
			{
				dayString = dayFormat.format(dateInMillis).toUpperCase();
			}
			else
			{

				dayString = dayFormat.format(dateInMillis);
			}
			return dayString;
		}
	}

	/**
	 * Converts db date format to the format "Month day", e.g "June 24".
	 *
	 * @param context      Context to use for resource localization
	 * @param dateInMillis The db formatted date string, expected to be of the form specified in
	 *                     Utility.DATE_FORMAT
	 * @return The day in the form of a string formatted "December 6"
	 */
	public static String getFormattedMonthDay(Context context, long dateInMillis)
	{
		Time time = new Time();
		time.setToNow();

		String currLocale = Locale.getDefault().getLanguage();

		SimpleDateFormat monthDayFormat;
		String monthDayString;

		if(currLocale.equals("ru"))
		{
			monthDayFormat = new SimpleDateFormat("dd MMMM");
			monthDayString = monthDayFormat.format(dateInMillis).toUpperCase();
		}
		else
		{
			monthDayFormat = new SimpleDateFormat("MMMM dd");
			monthDayString = monthDayFormat.format(dateInMillis);
		}

		return monthDayString;
	}
}