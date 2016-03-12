/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hally.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hally.sunshine.data.WeatherContract.LocationEntry;
import com.hally.sunshine.data.WeatherContract.WeatherEntry;
import com.hally.sunshine.util.TraceUtil;

import java.util.ArrayList;

/**
 * Manages a local database for weather data.
 */
public class WeatherDbHelper extends SQLiteOpenHelper
{
	static final String CLASS_NAME = WeatherDbHelper.class.getName();
	static final String DATABASE_NAME = "weather.db";
	// If you change the database schema, you must increment the database version.
	private static final int DATABASE_VERSION = 2;
	private static WeatherDbHelper _weatherDbHelper;
	private static boolean _isInstantiationAllowed = false;


	public WeatherDbHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static WeatherDbHelper getInstance(Context context)
	{
		if (_weatherDbHelper == null)
		{
			_isInstantiationAllowed = true;
			_weatherDbHelper = new WeatherDbHelper(context);
			_isInstantiationAllowed = false;
		}
		return _weatherDbHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase)
	{
		final String SQL_CREATE_WEATHER_TABLE = createWeatherTableSqlString();
		sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

		final String SQL_CREATE_LOCATION_TABLE = createLocationTableSqlString();
		sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
	}

	/**
	 * Creates SQLite string for creating a weather table
	 *
	 * @return
	 */
	private String createWeatherTableSqlString()
	{
		return "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
				WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

				// the ID of the location entry associated with this weather data
				WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
				WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
				WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
				WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

				WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
				WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

				WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
				WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
				WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
				WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

				// Set up the location column as a foreign key to location table.
				" FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
				LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

				// To assure the application have just one weather entry per day
				// per location, it's created a UNIQUE constraint with REPLACE strategy
				" UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " +
				WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
	}

	/**
	 * Creates SQLite string for creating a location table
	 *
	 * @return
	 */
	private String createLocationTableSqlString()
	{
		return "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
				LocationEntry._ID + " INTEGER PRIMARY KEY, " +
				LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
				LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
				LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
				LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " + ");";
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
	{
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		// Note that this only fires if you change the version number for your database.
		// It does NOT depend on the version number for your application.
		// If you want to update the schema without wiping data, commenting out the next 2 lines
		// should be your top priority before modifying this method.
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
		onCreate(sqLiteDatabase);
	}

	/**
	 * Added according to steps described
	 * https://github.com/sanathp/DatabaseManager_For_Android
	 * @param Query
	 * @return
	 */
	public ArrayList<Cursor> getData(String Query)
	{
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[]{"mesage"};
		//an array list of cursor to save two cursors one has results from the query
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2 = new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);

		try
		{
			String maxQuery = Query;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);

			//add value to cursor2
			Cursor2.addRow(new Object[]{"Success"});

			alc.set(1, Cursor2);
			if (null != c && c.getCount() > 0)
			{
				alc.set(0, c);
				c.moveToFirst();
				return alc;
			}
			return alc;
		}
		catch (SQLException sqlEx)
		{
			TraceUtil.logD(CLASS_NAME, "printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
			alc.set(1, Cursor2);
			return alc;
		}
		catch (Exception ex)
		{
			TraceUtil.logD(CLASS_NAME, "printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[]{"" + ex.getMessage()});
			alc.set(1, Cursor2);
			return alc;
		}
	}
}
