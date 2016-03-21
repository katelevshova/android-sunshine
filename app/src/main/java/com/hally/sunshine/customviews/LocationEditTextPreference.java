package com.hally.sunshine.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import com.hally.sunshine.R;
import com.hally.sunshine.util.TraceUtil;

/**
 * Created by Kateryna Levshova on 21.03.2016.
 */
public class LocationEditTextPreference extends EditTextPreference
{
	static public final String CLASS_NAME = LocationEditTextPreference.class.getName();
	static private final int MIN_DEFAULT_LENGTH = 2;
	static private final int MAX_DEFAULT_LENGTH = 12;
	private int _minLength = 0;
	private int _maxLength = 0;


	public LocationEditTextPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable
				.LocationEditTextPreference, 0, 0);

		try
		{
			_minLength = typedArray
					.getInt(R.styleable.LocationEditTextPreference_minLength, MIN_DEFAULT_LENGTH);
			_maxLength = typedArray
					.getInt(R.styleable.LocationEditTextPreference_maxLength, MAX_DEFAULT_LENGTH);
		}
		finally
		{
			//TypedArray objects are a shared resource and must be recycled after use.
			typedArray.recycle();
		}
	}

	@Override
	protected void showDialog(Bundle state)
	{
		super.showDialog(state);
		EditText editText = getEditText();

		editText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				Dialog dialog = getDialog();

				if (dialog instanceof AlertDialog)
				{
					Button positiveBtn = ((AlertDialog) getDialog()).getButton(AlertDialog
							.BUTTON_POSITIVE);

					int curLength = s.length();

					if (curLength < _minLength || curLength > _maxLength)
					{
						positiveBtn.setEnabled(false);
					}
					else
					{
						positiveBtn.setEnabled(true);
					}
				}
			}
		});
	}
}
