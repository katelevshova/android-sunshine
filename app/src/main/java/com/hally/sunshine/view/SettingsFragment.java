package com.hally.sunshine.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.hally.sunshine.R;
import com.hally.sunshine.data.WeatherContract;
import com.hally.sunshine.sync.SunshineSyncAdapter;
import com.hally.sunshine.util.Util;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design:
 * Settings</a> for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsFragment extends PreferenceFragment
		implements Preference.OnPreferenceChangeListener,
		SharedPreferences.OnSharedPreferenceChangeListener
{
	static public final String CLASS_NAME = SettingsFragment.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Add 'general' preferences, defined in the XML file
		addPreferencesFromResource(R.xml.settings_pref_general);

		// For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
		// updated when the preference changes.
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_art_pack_key)));
	}

	/**
	 * Attaches a listener so the summary is always updated with the preference value. Also fires
	 * the listener once, to initialize the summary (so it shows up before the value is changed.)
	 */
	private void bindPreferenceSummaryToValue(Preference preference)
	{
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(this);
		// Set the preference summaries
		setPreferenceSummary(preference,
				PreferenceManager
						.getDefaultSharedPreferences(preference.getContext())
						.getString(preference.getKey(), ""));
	}

	// Registers a shared preference change listener that gets notified when preferences change
	@Override
	public void onResume()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp.registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}

	// Unregisters a shared preference change listener
	@Override
	public void onPause()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	private void setPreferenceSummary(Preference preference, Object value)
	{
		String stringValue = value.toString();
		String key = preference.getKey();

		if (preference instanceof ListPreference)
		{
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate labels/values).
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0)
			{
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		}
		else if (key.equals(getString(R.string.pref_location_key)))
		{
			@SunshineSyncAdapter.LocationStatus int status = Util.getLocationStatus(getActivity());
			switch (status)
			{
				case SunshineSyncAdapter.LOCATION_STATUS_OK:
					preference.setSummary(stringValue);
					break;
				case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
					preference.setSummary(getString(R.string.pref_location_unknown_description, value.toString()));
					break;
				case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
					preference.setSummary(getString(R.string.pref_location_error_description, value.toString()));
					break;
				default:
					// Note --- if the server is down we still assume the value
					// is valid
					preference.setSummary(stringValue);
			}
		}
		else
		{
			// For other preferences, set the summary to the value's simple string representation.
			preference.setSummary(stringValue);
		}
	}

	// This gets called before the preference is changed
	@Override
	public boolean onPreferenceChange(Preference preference, Object value)
	{
		setPreferenceSummary(preference, value);
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(getString(R.string.pref_location_key)))
		{
			// we've changed the location
			// first clear locationStatus
			Util.resentLocationUnknown(getActivity());
			SunshineSyncAdapter.syncImmediately(getActivity());
		}
		else if (key.equals(getString(R.string.pref_units_key)))
		{
			// units have changed. update lists of weather entries accordingly
			getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry
					.CONTENT_URI, null);
		}
		else if (key.equals(getString(R.string.pref_location_status_key)))
		{
			// our location status has changed.  Update the summary accordingly
			Preference locationPreference = findPreference(getString(R.string.pref_location_key));
			bindPreferenceSummaryToValue(locationPreference);
		}
		else if(key.equals(R.string.pref_art_pack_key))
		{
			// art pack have changed. update lists of weather entries accordingly
			getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry
					.CONTENT_URI, null);
		}
	}
}