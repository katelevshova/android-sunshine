package com.hally.sunshine.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hally.sunshine.R;
import com.hally.sunshine.util.TraceUtil;

/**
 * @author Kateryna Levshova
 * @date 26.02.2015
 */
public class DetailFragment extends Fragment
{
	private final String CLASS_NAME = DetailFragment.class.getSimpleName();
	static final String FORECAST_STRING = "forecastForOneDay";
	private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
	private String _forecastStr;


	public DetailFragment()
	{
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		Intent intent = getActivity().getIntent();
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
		{
			_forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
			((TextView) rootView.findViewById(R.id.detail_text)).setText(_forecastStr);
		}

		return rootView;
	}

	//TODO: fix later - need to restore _forecastStr

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putString(FORECAST_STRING, _forecastStr);
		super.onSaveInstanceState(savedInstanceState);
	}

	//@Override
	/*public void onViewStateRestored(Bundle savedInstanceState)
	{
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState != null)
		{
			setForecastString(savedInstanceState.getString(FORECAST_STRING));
		}
	}*/

	private void setForecastString(String str)
	{
		((TextView) getActivity().findViewById(R.id.detail_text))
				.setText(str);
	}

	private Intent createShareForecastIntent()
	{
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		//return to your app in backstack
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, _forecastStr + FORECAST_SHARE_HASHTAG);
		return shareIntent;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_detail_fragment, menu);

		MenuItem itemShare = menu.findItem(R.id.item_share);

		ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat
			.getActionProvider(
				itemShare);

		if(shareActionProvider != null)
		{
			shareActionProvider.setShareIntent(createShareForecastIntent());
		}
		else
		{
			TraceUtil.logD(CLASS_NAME,"onCreateOptionsMenu", "Share Action provider is null?");
		}


	}

	/*@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();


		if (id == R.id.item_share)
		{
			ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat
					.getActionProvider(item);

			if(shareActionProvider != null)
			{
				shareActionProvider.setShareIntent(createShareForecastIntent());
			}
			else
			{
				TraceUtil.logD(CLASS_NAME,"onCreateOptionsMenu", "Share Action provider is null?");
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}*/
}
