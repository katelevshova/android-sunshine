package com.hally.sunshine.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Just an attempt to create own component
 * @author Kateryna Levshova
 * @date 03.04.2015
 */
public class CircleCustomView extends View
{
	private String _windSpeedDirection = "North West";

	public CircleCustomView(Context context)
	{
		super(context);
		init();
	}

	public CircleCustomView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		init();
	}

	public CircleCustomView(Context context, AttributeSet attributeSet, int defaultStyle)
	{
		super(context, attributeSet, defaultStyle);
		init();
	}

	private void init()
	{
		AccessibilityManager accessibilityManager = (AccessibilityManager) getContext()
				.getSystemService(Context.ACCESSIBILITY_SERVICE);

		if (accessibilityManager.isEnabled())
		{
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
		}
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event)
	{
		//TODO: fix this - issue #18
		event.getText().add(_windSpeedDirection);
		return super.dispatchPopulateAccessibilityEvent(event);
	}

/*	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event)
	{
		super.onPopulateAccessibilityEvent(event);
		event.getText().add(_windSpeedDirection);
	}*/

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		int x = getWidth() / 2;
		int y = getHeight() / 2;
		int radius = x;

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(Color.RED);
		canvas.drawCircle(x, y, radius, paint);
	}

	@Override
	protected void onMeasure(int widthMeasure, int heightMeasure)
	{
		int desiredWidth = 100;
		int desiredHeight = 100;

		int widthMode = MeasureSpec.getMode(widthMeasure);
		int widthSize = MeasureSpec.getSize(widthMeasure);
		int width = widthMode;

		if (widthMode == MeasureSpec.EXACTLY)//match_parent is EXACTLY + size of the parent
		{
			width = widthSize;
		}
		else if (widthMode == MeasureSpec.AT_MOST)    //wrap_content
		{
			width = Math.min(desiredWidth, widthSize);
		}
		else
		{
			width = desiredWidth;
		}

		//repeat for height
		int heightMode = MeasureSpec.getMode(heightMeasure);
		int heightSize = MeasureSpec.getSize(heightMeasure);
		int height = heightMode;

		if (heightMode == MeasureSpec.EXACTLY)//match_parent is EXACTLY + size of the parent
		{
			height = heightSize;
		}
		else if (heightMode == MeasureSpec.AT_MOST)    //wrap_content
		{

			height = Math.min(desiredHeight, heightSize);
		}
		else
		{
			height = desiredHeight;
		}

		setMeasuredDimension(width, height);
	}
}
