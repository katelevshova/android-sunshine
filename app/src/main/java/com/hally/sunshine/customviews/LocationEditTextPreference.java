package com.hally.sunshine.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.hally.sunshine.R;

/**
 * Created by Kateryna Levshova on 21.03.2016.
 */
public class LocationEditTextPreference extends EditTextPreference
{
	static private final int DEFAULT_LENGTH = 2;
	private int _minLength = 0;


	public LocationEditTextPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable
				.LocationEditTextPreference, 0, 0);

		try
		{
			_minLength = typedArray
					.getInt(R.styleable.LocationEditTextPreference_minLength, DEFAULT_LENGTH);
		}
		finally
		{
			//TypedArray objects are a shared resource and must be recycled after use.
			typedArray.recycle();
		}
	}
}
