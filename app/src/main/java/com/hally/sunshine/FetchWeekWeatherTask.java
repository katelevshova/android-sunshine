package com.hally.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.widget.ArrayAdapter;

import com.hally.sunshine.util.TraceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * @author Kateryna Levshova
 * @date 24.02.2015
 */
public class FetchWeekWeatherTask extends AsyncTask<String, Void, String[]>
{
	private final String CLASS_NAME = FetchWeekWeatherTask.class.getSimpleName();
	private ArrayAdapter<String> _forecastAdapter;

	/**
	 * Constructor
	 *
	 * @param forecastAdapter
	 */
	public FetchWeekWeatherTask(ArrayAdapter<String> forecastAdapter)
	{
		_forecastAdapter = forecastAdapter;
	}

	/**
	 * The date/time conversion code is going to be moved outside the asynctask later, so for
	 * convenience we're breaking it out into its own method now.
	 *
	 * @param time
	 */
	private String getReadableDateString(long time)
	{
		// Because the API returns a unix timestamp (measured in seconds),
		// it must be converted to milliseconds in order to be converted to valid date.
		SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
		return shortenedDateFormat.format(time);
	}

	/**
	 * Prepare the weather high/lows for presentation.
	 *
	 * @param high
	 * @param low
	 */
	private String formatHighLows(double high, double low)
	{
		// For presentation, assume the user doesn't care about tenths of a degree.
		long roundedHigh = Math.round(high);
		long roundedLow = Math.round(low);
		String highLowStr = roundedHigh + "/" + roundedLow;
		return highLowStr;
	}

	/**
	 * Take the String representing the complete forecast in JSON Format and pull out the data we
	 * need to construct the Strings needed for the wireframes.
	 *
	 * @param forecastJsonStr
	 * @param numDays
	 */
	private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
			throws JSONException
	{
		// These are the names of the JSON objects that need to be extracted.
		final String JO_LIST = "list";
		final String JO_WEATHER = "weather";
		final String JO_TEMPERATURE = "temp";
		final String JO_MAX = "max";
		final String JO_MIN = "min";
		final String JO_DESCRIPTION = "main";
		JSONObject forecastJson = new JSONObject(forecastJsonStr);
		JSONArray weatherArray = forecastJson.getJSONArray(JO_LIST);

		// JO returns daily forecasts based upon the local time of the city that is being
		// asked for, which means that we need to know the GMT offset to translate this data
		// properly.
		// Since this data is also sent in-order and the first day is always the
		// current day, we're going to take advantage of that to get a nice
		// normalized UTC date for all of our weather.
		Time dayTime = new Time();
		dayTime.setToNow();

		// we start at the day returned by local time. Otherwise this is a mess.
		int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

		// now we work exclusively in UTC
		dayTime = new Time();
		String[] resultStrs = new String[numDays];

		for (int i = 0; i < weatherArray.length(); i++)
		{
			// For now, using the format "Day, description, hi/low"
			String day;
			String description;
			String highAndLow;

			// Get the JSON object representing the day
			JSONObject dayForecast = weatherArray.getJSONObject(i);

			// The date/time is returned as a long. We need to convert that
			// into something human-readable, since most people won't read "1400356800" as
			// "this saturday".
			long dateTime;

			// Cheating to convert this to UTC time, which is what we want anyhow
			dateTime = dayTime.setJulianDay(julianStartDay + i);
			day = getReadableDateString(dateTime);

			// description is in a child array called "weather", which is 1 element long.
			JSONObject weatherObject = dayForecast.getJSONArray(JO_WEATHER).getJSONObject(0);
			description = weatherObject.getString(JO_DESCRIPTION);

			// Temperatures are in a child object called "temp". Try not to name variables
			// "temp" when working with temperature. It confuses everybody.
			JSONObject temperatureObject = dayForecast.getJSONObject(JO_TEMPERATURE);
			double high = temperatureObject.getDouble(JO_MAX);
			double low = temperatureObject.getDouble(JO_MIN);
			highAndLow = formatHighLows(high, low);
			resultStrs[i] = day + " - " + description + " - " + highAndLow;
		}

		for (String s : resultStrs)
		{
			TraceUtil.logV(CLASS_NAME, "getWeatherDataFromJson", "Forecast entry: " + s);
		}
		return resultStrs;
	}

	@Override
	protected String[] doInBackground(String... params)
	{
		// If there's no zip code, there's nothing to look up. Verify size of params.
		if (params.length == 0)
		{
			return null;
		}

		// These two need to be declared outside the try/catch
		// so that they can be closed in the finally block.
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		// Will contain the raw JSON response as a string.
		String forecastJsonStr = null;

		//TODO: move this to special utility class later (UriBuilder.java)
		String format = "json";
		String units = "metrics";
		int numDays = 7;

		try
		{
			// Construct the URL for the OpenWeatherMap query
			// Possible parameters are avaiable at OWM's forecast API page, at
			// http://openweathermap.org/API#forecast

			/*URL url =
					new URL("http://api.openweathermap.org/data/2" +
				".5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
			*/
			final String FORECSE_BASE_URL = "http://api.openweathermap.org/data/2" +
					".5/forecast/daily?";
			final String QUERY_PARAM = "q";
			final String FORMAT_PARAM = "mode";
			final String UNITS_PARAM = "units";
			final String DAYS_PARAM = "cnt";
			String postalCode = params[0];

			Uri builtUri = Uri.parse(FORECSE_BASE_URL).buildUpon()
					.appendQueryParameter(QUERY_PARAM, postalCode)
					.appendQueryParameter(FORMAT_PARAM, format)
					.appendQueryParameter(UNITS_PARAM, units)
					.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).build();

			URL url = new URL(builtUri.toString());
			TraceUtil.logD(CLASS_NAME, "doInBackground", "builtUri= " + builtUri);


			// Create the request to OpenWeatherMap, and open the connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			// Read the input stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();

			if (inputStream == null)
			{
				return null; // Nothing to do.
			}

			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null)
			{
				// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
				// But it does make debugging a *lot* easier if you print out the completed
				// buffer for debugging.
				buffer.append(line + "\n");
			}
			if (buffer.length() == 0)
			{
				return null; // Stream was empty. No point in parsing.
			}
			forecastJsonStr = buffer.toString();
			TraceUtil.logD(CLASS_NAME, "doInBackground", "forecastJsonStr= " + forecastJsonStr);
		}
		catch (IOException e)
		{
			TraceUtil.logE(CLASS_NAME, "doInBackground", "Error ", e);
			// If the code didn't successfully get the weather data, there's no point in attemping
			// to parse it.
			return null;
		}
		finally
		{
			if (urlConnection != null)
			{
				urlConnection.disconnect();
			}
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (final IOException e)
				{
					TraceUtil.logE(CLASS_NAME, "doInBackground", "Error closing stream", e);
				}
			}
		}

		try
		{
			return getWeatherDataFromJson(forecastJsonStr, numDays);
		}
		catch (JSONException e)
		{
			TraceUtil.logE(CLASS_NAME,"doInBackground", e.getMessage(), e);
			e.printStackTrace();
		}

		// This will only happen if there was an error getting or parsing the forecast.
		return null;
	}
}
