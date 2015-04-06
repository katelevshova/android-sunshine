package com.hally.sunshine.service;

import android.app.IntentService;
import android.content.Intent;
import android.widget.ArrayAdapter;

/**
 * @author Kateryna Levshova
 * @date 03.04.2015
 */
public class SunshineService extends IntentService
{
	private static final String CLASS_NAME = SunshineService.class.getSimpleName();
	private ArrayAdapter<String> _forecastAdaoter;



	public SunshineService()
	{
		super(CLASS_NAME);
	}


	@Override
	protected void onHandleIntent(Intent intent)
	{

	}



}
