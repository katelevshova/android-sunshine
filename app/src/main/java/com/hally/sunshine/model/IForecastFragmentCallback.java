package com.hally.sunshine.model;

import android.net.Uri;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 * @date 27.03.2015
 */
public interface IForecastFragmentCallback
{
	/**
	 * DetailFragmentCallback for when an item has been selected.
	 */
	public void onItemSelected(Uri dateUri);
}
