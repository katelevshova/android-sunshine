package com.hally.sunshine.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Kateryna Levshova
 * @date 24.02.2015
 */
//TODO: currently is not used. Refactor FetchWeekWeatherTask
public class WeatherDataParser
{
	/**
	 * Given a string of the form returned by the api call:
	 * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
	 * retrieve the maximum temperature for the day indicated by dayIndex (Note: 0-indexed, so 0
	 * would refer to the first day).
	 */
	public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
			throws JSONException
	{
		// These are the names of the JSON objects that need to be extracted.
		final String JO_LIST = "list";
		final String JO_TEMPERATURE = "temp";
		final String JO_MAX = "max";

		JSONObject forecastJson = new JSONObject(weatherJsonStr);
		JSONArray daysWeatherArray = forecastJson.getJSONArray(JO_LIST);

		// Get the JSON object representing the day
		JSONObject dayObject = daysWeatherArray.getJSONObject(dayIndex);
		JSONObject temperatureObject = dayObject.getJSONObject(JO_TEMPERATURE);
		double maxTemperature = temperatureObject.getDouble(JO_MAX);

		return maxTemperature;

	}
}
