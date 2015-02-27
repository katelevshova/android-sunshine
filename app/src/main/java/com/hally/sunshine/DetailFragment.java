package com.hally.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Kateryna Levshova
 * @date 26.02.2015
 */
public class DetailFragment extends Fragment
{
	public DetailFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		Intent intent = getActivity().getIntent();
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
		{
			String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
			((TextView) rootView.findViewById(R.id.detail_text)).setText(forecastStr);
		}

		return rootView;
	}
}
