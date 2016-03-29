package com.hally.sunshine.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Kateryna Levshova on 28.03.2016.
 */
public class SunshineInstanceIDListenerService extends InstanceIDListenerService
{
	private static final String CLASS_NAME = SunshineInstanceIDListenerService.class.getSimpleName();

	/**
	 * Called if InstanceID token is updated. This may occur if the security of the previous
	 * token had been compromised. this call is initiated by the InstanceID provider.
	 */
	@Override
	public void onTokenRefresh()
	{
		//Fetch updated Instance id token
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}


}
