package com.hally.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hally.sunshine.R;
import com.hally.sunshine.util.TraceUtil;
import com.hally.sunshine.view.MainForecastActivity;

/**
 * Created by Kateryna Levshova on 28.03.2016.
 */
public class MyRegistrationIntentService extends IntentService
{
	private static final String CLASS_NAME = MyRegistrationIntentService.class.getSimpleName();

	public MyRegistrationIntentService()
	{
		super(CLASS_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		try
		{
			// In the (unlikely) event that multiple refresh operations occur simultaneously,
			// ensure that they are processed sequentially.
			synchronized (CLASS_NAME)
			{
				// Initially this call goes out to the network to retrieve the token, subsequent calls
				// are local.
				InstanceID instanceID = InstanceID.getInstance(this);
				String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
						GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
				sendRegistrationToServer(token);

				// You should store a boolean that indicates whether the generated token has been
				// sent to your server. If the boolean is false, send the token to your server,
				// otherwise your server should have already received the token.
				sharedPreferences.edit().putBoolean(MainForecastActivity.SENT_TOKEN_TO_SERVER, true)
						.apply();
			}
		}
		catch (Exception e)
		{
			TraceUtil.logD(CLASS_NAME, "onHandleIntent", "Failed to complete token refresh ", e);

			// If an exception happens while fetching the new token or updating our registration data
			// on a third-party server, this ensures that we'll attempt the update at a later time.
			sharedPreferences.edit().putBoolean(MainForecastActivity.SENT_TOKEN_TO_SERVER, false)
					.apply();
		}
	}

	/**
	 * Normally, you would want to persist the registration to third-party servers. Because we do
	 * not have a server, and are faking it with a website, you'll want to log the token instead.
	 * That way you can see the value in logcat, and note it for future use in the website.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token)
	{
		TraceUtil.logI(CLASS_NAME, "sendRegistrationToServer", "GCM Registration Token: " + token);
	}
}

