package com.hally.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;

import com.bumptech.glide.Glide;
import com.hally.sunshine.BuildConfig;
import com.hally.sunshine.R;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.util.FormatUtil;
import com.hally.sunshine.util.ImageResouceUtil;
import com.hally.sunshine.util.TraceUtil;
import com.hally.sunshine.util.Util;
import com.hally.sunshine.view.MainForecastActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{
	public final static String CLASS_NAME = SunshineSyncAdapter.class.getSimpleName();
	public static final int SYNC_INTERVAL = 60 * 180; // 3 hour = 10800 sec
	public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
	public static final int LOCATION_STATUS_OK = 0;
	public static final int LOCATION_STATUS_SERVER_DOWN = 1;
	public static final int LOCATION_STATUS_SERVER_INVALID = 2;
	public static final int LOCATION_STATUS_UNKNOWN = 3;
	public static final int LOCATION_STATUS_INVALID = 4;
	private static final int NUMBER_DAYS = 14;
	private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
	};
	// these indices must match the projection
	private static final int INDEX_WEATHER_ID = 0;
	private static final int INDEX_MAX_TEMP = 1;
	private static final int INDEX_MIN_TEMP = 2;
	private static final int INDEX_SHORT_DESC = 3;
	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final int WEATHER_NOTIFICATION_ID = 3004;

	public SunshineSyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
	}

	/**
	 * Helper method to have the sync adapter sync immediately
	 *
	 * @param context The context used to access the account service
	 */
	public static void syncImmediately(Context context)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(context),
				context.getString(R.string.content_authority), bundle);
	}

	/**
	 * Helper method to get the fake account to be used with SyncAdapter, or make a new one if the
	 * fake account doesn't exist yet.  If we make a new account, we call the onAccountCreated
	 * method so we can initialize things.
	 *
	 * @param context The context used to access the account service
	 * @return a fake account.
	 */
	public static Account getSyncAccount(Context context)
	{
		// Get an instance of the Android account manager
		AccountManager accountManager =
				(AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		// Create the account type and default account
		Account newAccount = new Account(
				context.getString(R.string.app_name),
				context.getString(R.string.sync_account_type));

		// If the password doesn't exist, the account doesn't exist
		if (null == accountManager.getPassword(newAccount))
		{

        /*
		 * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
			if (!accountManager.addAccountExplicitly(newAccount, "", null))
			{
				return null;
			}
			/*
			 * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

			onAccountCreated(newAccount, context);
		}
		return newAccount;
	}

	/**
	 * Helper method to schedule the sync adapter periodic execution
	 */
	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime)
	{
		Account account = getSyncAccount(context);
		String authority = context.getString(R.string.content_authority);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					syncPeriodic(syncInterval, flexTime).
					setSyncAdapter(account, authority).
					setExtras(new Bundle()).build();
			ContentResolver.requestSync(request);
		}
		else
		{
			ContentResolver.addPeriodicSync(account,
					authority, new Bundle(), syncInterval);
		}
	}

	private static void onAccountCreated(Account newAccount, Context context)
	{
		/*
         * Since we've created an account
         */
		SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
		ContentResolver
				.setSyncAutomatically(newAccount, context.getString(R.string.content_authority),
						true);

        /*
         * Finally, let's do a sync to get things started
         */
		syncImmediately(context);
	}

	public static void initializeSyncAdapter(Context context)
	{
		getSyncAccount(context);
	}

	/**
	 * Dummy JSON data {"cod":"200","message":0.0268,"city":{"id":0,"name":"Mountain
	 * View","country":"US","coord":{"lat":37.4056,"lon":-122.0775}},"cnt":14,"list":[{"dt":1428436800,"temp":{"day":9.97,"min":3.24,"max":12.44,"night":3.86,"eve":7.9,"morn":10.38},"pressure":989.22,"humidity":94,"weather":[{"id":501,"main":"Rain","description":"moderate
	 * rain","icon":"10d"}],"speed":1.9,"deg":249,"clouds":92,"rain":9.05},{"dt":1428523200,"temp":{"day":12.75,"min":0.92,"max":13.56,"night":0.92,"eve":9.26,"morn":7.43},"pressure":995.15,"humidity":78,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":1.41,"deg":322,"clouds":24,"rain":0.73},{"dt":1428609600,"temp":{"day":17.31,"min":2.58,"max":17.64,"night":3.15,"eve":12.48,"morn":2.58},"pressure":992.46,"humidity":59,"weather":[{"id":801,"main":"Clouds","description":"few
	 * clouds","icon":"02d"}],"speed":1.36,"deg":320,"clouds":12},{"dt":1428696000,"temp":{"day":14.02,"min":7,"max":16.48,"night":8.67,"eve":16.48,"morn":7},"pressure":1007.81,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky
	 * is clear","icon":"01d"}],"speed":0.87,"deg":276,"clouds":6},{"dt":1428782400,"temp":{"day":13.69,"min":7.57,"max":14.39,"night":9.66,"eve":14.39,"morn":7.57},"pressure":1010.35,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":1.33,"deg":264,"clouds":91,"rain":1.28},{"dt":1428868800,"temp":{"day":13.58,"min":7.18,"max":15.57,"night":10.26,"eve":15.57,"morn":7.18},"pressure":1013.46,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky
	 * is clear","icon":"01d"}],"speed":4.51,"deg":331,"clouds":0},{"dt":1428955200,"temp":{"day":15.24,"min":7,"max":16.77,"night":10.31,"eve":16.77,"morn":7},"pressure":1011.39,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky
	 * is clear","icon":"01d"}],"speed":3.14,"deg":359,"clouds":0},{"dt":1429041600,"temp":{"day":14.5,"min":6.07,"max":15.51,"night":8.72,"eve":15.51,"morn":6.07},"pressure":1011.2,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":1.03,"deg":220,"clouds":0},{"dt":1429128000,"temp":{"day":14.85,"min":7.01,"max":16.36,"night":10.22,"eve":16.36,"morn":7.01},"pressure":1011.8,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":1.98,"deg":305,"clouds":2},{"dt":1429214400,"temp":{"day":15.11,"min":7.84,"max":16.02,"night":8.86,"eve":16.02,"morn":7.84},"pressure":1010.11,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky
	 * is clear","icon":"01d"}],"speed":2.3,"deg":280,"clouds":0},{"dt":1429300800,"temp":{"day":15.89,"min":6.8,"max":16.96,"night":8.77,"eve":16.96,"morn":6.8},"pressure":1009.03,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":2.15,"deg":276,"clouds":0},{"dt":1429387200,"temp":{"day":13.08,"min":6.68,"max":14.75,"night":10.63,"eve":14.75,"morn":6.68},"pressure":1006.95,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light
	 * rain","icon":"10d"}],"speed":1.72,"deg":253,"clouds":61,"rain":2.04},{"dt":1429473600,"temp":{"day":12.54,"min":10.75,"max":12.59,"night":11.15,"eve":12.59,"morn":10.75},"pressure":995.58,"humidity":0,"weather":[{"id":502,"main":"Rain","description":"heavy
	 * intensity rain","icon":"10d"}],"speed":6.4,"deg":173,"clouds":58,"rain":28.37},{"dt":1429560000,"temp":{"day":12.02,"min":9.96,"max":12.66,"night":9.96,"eve":12.66,"morn":10.15},"pressure":1003.39,"humidity":0,"weather":[{"id":501,"main":"Rain","description":"moderate
	 * rain","icon":"10d"}],"speed":3.03,"deg":268,"clouds":44,"rain":4.29}]}
	 */


	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
							  ContentProviderClient provider, SyncResult syncResult)
	{
		TraceUtil.logD(CLASS_NAME, "onPerformSync", "Called.");

		String locationQuery = Util.getPreferredLocation(getContext());

		// These two need to be declared outside the try/catch
		// so that they can be closed in the finally block.
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		// Will contain the raw JSON response as a string.
		String forecastJsonStr = null;

		String format = "json";
		String units = "metric";
		int numDays = NUMBER_DAYS;

		try
		{
			// Construct the URL for the OpenWeatherMap query
			// Possible parameters are avaiable at OWM's forecast API page, at
			// http://openweathermap.org/API#forecast
			final String FORECAST_BASE_URL =
					"http://api.openweathermap.org/data/2.5/forecast/daily?";
			final String QUERY_PARAM = "q";
			final String FORMAT_PARAM = "mode";
			final String UNITS_PARAM = "units";
			final String DAYS_PARAM = "cnt";
			final String APPID_PARAM = "APPID";

			Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
					.appendQueryParameter(QUERY_PARAM, locationQuery)
					.appendQueryParameter(FORMAT_PARAM, format)
					.appendQueryParameter(UNITS_PARAM, units)
					.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
					.appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
					.build();

			URL url = new URL(builtUri.toString());

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
				return;
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
				// Stream was empty.  No point in parsing.
				TraceUtil.logI(CLASS_NAME, "onHandleIntent", "Stream was empty.");
				Util.setLocationStatusPref(getContext(), LOCATION_STATUS_SERVER_DOWN);
				return;
			}
			forecastJsonStr = buffer.toString();

			getWeatherDataFromJson(forecastJsonStr, locationQuery);
		}
		catch (IOException e)
		{
			// If the code didn't successfully get the weather data, there's no point in attempting
			// to parse it.
			TraceUtil.logE(CLASS_NAME, "onHandleIntent", e.getMessage(), e);
			Util.setLocationStatusPref(getContext(), LOCATION_STATUS_SERVER_DOWN);
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			TraceUtil.logE(CLASS_NAME, "onHandleIntent", e.getMessage(), e);
			Util.setLocationStatusPref(getContext(), LOCATION_STATUS_SERVER_INVALID);
			e.printStackTrace();
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
					TraceUtil.logE(CLASS_NAME, "onHandleIntent", "Error closing stream", e);
				}
			}
		}
		return;

	}

	/**
	 * Take the String representing the complete forecast in JSON Format and pull out the data we
	 * need to construct the Strings needed for the wireframes.
	 * <p/>
	 * Fortunately parsing is easy:  constructor takes the JSON string and converts it into an
	 * Object hierarchy for us.
	 */
	private void getWeatherDataFromJson(String forecastJsonStr,
										String locationSetting)
			throws JSONException
	{

		// Now we have a String representing the complete forecast in JSON Format.
		// Fortunately parsing is easy:  constructor takes the JSON string and converts it
		// into an Object hierarchy for us.

		// These are the names of the JSON objects that need to be extracted.

		// Location information
		final String OWM_CITY = "city";
		final String OWM_CITY_NAME = "name";
		final String OWM_COORD = "coord";

		// Location coordinate
		final String OWM_LATITUDE = "lat";
		final String OWM_LONGITUDE = "lon";

		// Weather information.  Each day's forecast info is an element of the "list" array.
		final String OWM_LIST = "list";

		final String OWM_PRESSURE = "pressure";
		final String OWM_HUMIDITY = "humidity";
		final String OWM_WINDSPEED = "speed";
		final String OWM_WIND_DIRECTION = "deg";

		// All temperatures are children of the "temp" object.
		final String OWM_TEMPERATURE = "temp";
		final String OWM_MAX = "max";
		final String OWM_MIN = "min";

		final String OWM_WEATHER = "weather";
		final String OWM_DESCRIPTION = "main";
		final String OWM_WEATHER_ID = "id";
		final String OWM_MESSAGE_CODE = "cod";

		try
		{
			JSONObject forecastJson = new JSONObject(forecastJsonStr);
			JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

			JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
			String cityName = cityJson.getString(OWM_CITY_NAME);

			JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
			double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
			double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

			if (forecastJson.has(OWM_MESSAGE_CODE))
			{
				int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

				switch (errorCode)
				{
					case HttpURLConnection.HTTP_OK:
						break;
					case HttpURLConnection.HTTP_NOT_FOUND:
						Util.setLocationStatusPref(getContext(), LOCATION_STATUS_INVALID);
						return;
					default:
						Util.setLocationStatusPref(getContext(), LOCATION_STATUS_SERVER_DOWN);
						return;
				}
			}

			long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

			// Insert the new weather information into the database
			Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

			// OWM returns daily forecasts based upon the local time of the city that is being
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

			for (int i = 0; i < weatherArray.length(); i++)
			{
				// These are the values that will be collected.
				long dateTime;
				double pressure;
				int humidity;
				double windSpeed;
				double windDirection;

				double high;
				double low;

				String description;
				int weatherId;

				// Get the JSON object representing the day
				JSONObject dayForecast = weatherArray.getJSONObject(i);

				// Cheating to convert this to UTC time, which is what we want anyhow
				dateTime = dayTime.setJulianDay(julianStartDay + i);

				pressure = dayForecast.getDouble(OWM_PRESSURE);
				humidity = dayForecast.getInt(OWM_HUMIDITY);
				windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
				windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

				// Description is in a child array called "weather", which is 1 element long.
				// That element also contains a weather code.
				JSONObject weatherObject =
						dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
				description = weatherObject.getString(OWM_DESCRIPTION);
				weatherId = weatherObject.getInt(OWM_WEATHER_ID);

				// Temperatures are in a child object called "temp".  Try not to name variables
				// "temp" when working with temperature.  It confuses everybody.
				JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
				high = temperatureObject.getDouble(OWM_MAX);
				low = temperatureObject.getDouble(OWM_MIN);

				ContentValues weatherValues = new ContentValues();

				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
				weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

				cVVector.add(weatherValues);
			}

			int inserted = 0;
			// add to database
			if (cVVector.size() > 0)
			{
				ContentValues[] cvArray = new ContentValues[cVVector.size()];
				cVVector.toArray(cvArray);
				getContext().getContentResolver()
						.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

				deleteOldData(dayTime, julianStartDay);

				notifyWeather();
			}

			Util.setLocationStatusPref(getContext(), LOCATION_STATUS_OK);
			TraceUtil.logD(CLASS_NAME, "getWeatherDataFromJson",
					"Sunshine Service Complete. " + cVVector.size() + " " +
							"Inserted");
		}
		catch (JSONException e)
		{
			TraceUtil.logE(CLASS_NAME, "getWeatherDataFromJson", e.getMessage(), e);
			Util.setLocationStatusPref(getContext(), LOCATION_STATUS_SERVER_INVALID);
			e.printStackTrace();
		}
	}

	private void deleteOldData(Time dayTime, int julianStartDay)
	{
		getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
				WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
				new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});
	}

	/**
	 * Helper method to handle insertion of a new location in the weather database.
	 *
	 * @param locationSetting The location string used to request updates from the server.
	 * @param cityName        A human-readable city name, e.g "Mountain View"
	 * @param lat             the latitude of the city
	 * @param lon             the longitude of the city
	 * @return the row ID of the added location.
	 */
	long addLocation(String locationSetting, String cityName, double lat, double lon)
	{
		long locationId;

		// First, check if the location with this city name exists in the db
		Cursor locationCursor = getContext().getContentResolver().query(
				WeatherContract.LocationEntry.CONTENT_URI,
				new String[]{WeatherContract.LocationEntry._ID},
				WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
				new String[]{locationSetting},
				null);

		if (locationCursor.moveToFirst())
		{
			int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
			locationId = locationCursor.getLong(locationIdIndex);
		}
		else
		{
			// Now that the content provider is set up, inserting rows of data is pretty simple.
			// First create a ContentValues object to hold the data you want to insert.
			ContentValues locationValues = new ContentValues();

			// Then add the data, along with the corresponding name of the data type,
			// so the content provider knows what kind of value is being inserted.
			locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
			locationValues
					.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
			locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
			locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

			// Finally, insert location data into the database.
			Uri insertedUri = getContext().getContentResolver().insert(
					WeatherContract.LocationEntry.CONTENT_URI,
					locationValues
			);

			// The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
			locationId = ContentUris.parseId(insertedUri);
		}

		locationCursor.close();
		// Wait, that worked?  Yes!
		return locationId;
	}

	private void notifyWeather()
	{
		Context context = getContext();
		//checking the last update and notify if it' the first of the day
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String lastNotificationKey = context.getString(R.string.pref_last_notification);
		long lastSync = prefs.getLong(lastNotificationKey, 0);

		if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS)
		{
			// Last sync was more than 1 day ago, let's send a notification with the weather.
			String locationQuery = Util.getPreferredLocation(context);

			Uri weatherUri = WeatherContract.WeatherEntry
					.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

			// we'll query our contentProvider, as always
			Cursor cursor = context.getContentResolver()
					.query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

			if (cursor.moveToFirst())
			{
				int weatherId = cursor.getInt(INDEX_WEATHER_ID);
				double high = cursor.getDouble(INDEX_MAX_TEMP);
				double low = cursor.getDouble(INDEX_MIN_TEMP);
				String desc = cursor.getString(INDEX_SHORT_DESC);

				Resources resources = context.getResources();
				int iconId = ImageResouceUtil.getIconResourceForWeatherCondition(weatherId);
				int artResourceId = ImageResouceUtil.getArtResourceForWeatherCondition(weatherId);
				String artUrl = ImageResouceUtil.getArtUrlForWeatherCondition(context, weatherId);

				@SuppressLint("InlinedApi")
				int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
						? resources
						.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
						: resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
				@SuppressLint("InlinedApi")
				int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
						? resources
						.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
						: resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

				// Retrieve the large icon
				Bitmap largeIcon;
				try
				{
					largeIcon = Glide.with(context).load(artUrl).asBitmap().error(artResourceId)
							.fitCenter().into(largeIconWidth, largeIconHeight).get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					TraceUtil.logE(CLASS_NAME, "notifyWeather",
							"Error retrieving large icon from " + artUrl, e);
					largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
				}

				String title = context.getString(R.string.app_name);

				// Define the text of the forecast.
				String contentText = String.format(context.getString(R.string.notification_format),
						desc,
						FormatUtil.formatTemperature(context, high),
						FormatUtil.formatTemperature(context, low));

				boolean isNotificationOn = prefs.getBoolean(getContext().getString(R.string
						.pref_enable_notifications_key), Boolean.parseBoolean(getContext()
						.getString(R.string.pref_enable_notifications_default)));

				if (isNotificationOn)
				{
					//build your notification here.
					showNotification(iconId, largeIcon, title, contentText);
				}

				//refreshing last sync
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(lastNotificationKey, System.currentTimeMillis());
				editor.commit();
			}
			cursor.close();
		}
	}

	/**
	 * Builds notification
	 * @param smallIconId
	 * @param largeIcon
	 * @param title
	 * @param contentText
	 */
	private void showNotification(int smallIconId, Bitmap largeIcon, String title, String
			contentText)
	{
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder
				(getContext())
		.setSmallIcon(smallIconId)
		.setLargeIcon(largeIcon)
		.setContentTitle(title)
		.setContentText(contentText);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			notificationBuilder.setColor(ContextCompat.getColor(getContext(), R.color.light_blue));
		}
		else
		{
			notificationBuilder.setColor(getContext().getResources().getColor(R.color.light_blue));
		}

		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		Intent resultIntent = new Intent(getContext(), MainForecastActivity.class);

		TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getContext());
		taskStackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);

		notificationBuilder.setContentIntent(resultPendingIntent);


		NotificationManager notificationManager = (NotificationManager)
				getContext().getSystemService(Context.NOTIFICATION_SERVICE);

		// WEATHER_NOTIFICATION_ID allows you to update the notification later on.
		notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());
	}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,
			LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID})
	public @interface LocationStatus
	{
	}

}