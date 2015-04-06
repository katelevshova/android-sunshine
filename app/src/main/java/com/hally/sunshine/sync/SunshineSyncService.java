package com.hally.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hally.sunshine.util.TraceUtil;

public class SunshineSyncService extends Service
{
	public final static String CLASS_NAME = SunshineSyncAdapter.class.getSimpleName();
	private static final Object sSyncAdapterLock = new Object();
	private static SunshineSyncAdapter sSunshineSyncAdapter = null;

	@Override
	public void onCreate()
	{
		TraceUtil.logD(CLASS_NAME, "onCreate", "called");
		synchronized (sSyncAdapterLock)
		{
			if (sSunshineSyncAdapter == null)
			{
				sSunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return sSunshineSyncAdapter.getSyncAdapterBinder();
	}
}