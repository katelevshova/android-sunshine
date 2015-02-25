package com.hally.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Kateryna Levshova
 * @date 24.02.2015
 */
public class FetchWeekWeatherTask extends AsyncTask<String, Void, Void>
{
	private final String CLASS_NAME = FetchWeekWeatherTask.class.getSimpleName();

	@Override
	protected Void doInBackground(String... params)
	{

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
			Log.v(CLASS_NAME, "builtUri= "+builtUri);


			// Create the request to OpenWeatherMap, and open the connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			// Read the input stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();

			if (inputStream == null)
			{
				// Nothing to do.
				return null;
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
				// Stream was empty. No point in parsing.
				return null;
			}
			forecastJsonStr = buffer.toString();
			Log.d(CLASS_NAME, "forecastJsonStr= " + forecastJsonStr);
		}
		catch (IOException e)
		{
			Log.e(CLASS_NAME, "Error ", e);
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
					Log.e(CLASS_NAME, "Error closing stream", e);
				}
			}
		}

		return null;
	}
}
