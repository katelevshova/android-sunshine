package com.hally.sunshine.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hally.sunshine.R;

/**
 * @author Kateryna Levshova
 * @date 26.02.2015
 */
public class DetailFragment extends Fragment
{
	static final String FORECAST_STRING = "forecastForOneDay";
	private String _forecastStr;


	public DetailFragment()
	{
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
}
