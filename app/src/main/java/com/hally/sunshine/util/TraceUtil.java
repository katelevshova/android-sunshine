package com.hally.sunshine.util;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * Singleton class enables to turn ON/OFF presenting messages in LogCat console
 *
 * @author Kateryna Levshova
 * @date 21.11.2014
 */
public class TraceUtil
{
	private static final String CLASS_NAME = "TraceUtil";
	private static final String methodSeparator = "->";
	private static final String classSeparator1 = " [";
	private static final String classSeparator2 = "]";
	// NOTE: cannot be moved into settings.xml file because of security reasons
	private static boolean IS_DEBUG_MODE_ENABLED = true; //TODO: changed it to false before release

	private static TraceUtil _traceUtilInst;
	private static boolean _isInstantiationAllowed = false;

	public static TraceUtil getInstance()
	{
		if (_traceUtilInst == null)
		{
			_isInstantiationAllowed = true;
			_traceUtilInst = new TraceUtil();
			_isInstantiationAllowed = false;
		}
		return _traceUtilInst;
	}

	/**
	 * Sends a log message if <code>IS_DEBUG_MODE_ENABLED=true</code>.
	 *
	 * @param methodName Used to identify the source of a log message.  It usually identifies the
	 *                   class or activity where the log call occurs.
	 * @param msg        The message you would like logged.
	 */
	public static void logD(String className, String methodName, String msg)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.d(classSeparator1 + className + classSeparator2 + methodSeparator + methodName,
					msg);
		}
	}

	/**
	 * Sends a log message if <code>IS_DEBUG_MODE_ENABLED=true</code>.
	 *
	 * @param methodName Used to identify the source of a log message.  It usually identifies the
	 *                   class or activity where the log call occurs.
	 * @param msg        The message you would like logged.
	 * @param tr         An exception to log
	 */
	public static void logD(String className, String methodName, String msg, Throwable tr)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.d(classSeparator1 + className + classSeparator2 + methodSeparator + methodName, msg,
					tr);
		}
	}

	public static void logE(String className, String methodName, String msg)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.e(classSeparator1 + className + classSeparator2 + methodSeparator + methodName,
					msg);
		}
	}

	public static void logE(String className, String methodName, String msg, Throwable tr)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.e(classSeparator1 + className + classSeparator2 + methodSeparator + methodName, msg,
					tr);
		}
	}

	public static void logW(String className, String methodName, String msg)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.w(classSeparator1 + className + classSeparator2 + methodSeparator + methodName,
					msg);
		}
	}

	public static void logW(String className, String methodName, String msg, Throwable tr)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.w(classSeparator1 + className + classSeparator2 + methodSeparator + methodName, msg,
					tr);
		}
	}

	public static void logI(String className, String methodName, String msg)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.i(classSeparator1 + className + classSeparator2 + methodSeparator + methodName,
					msg);
		}
	}

	public static void logI(String className, String methodName, String msg, Throwable tr)
	{
		if (IS_DEBUG_MODE_ENABLED)
		{
			Log.i(classSeparator1 + className + classSeparator2 + methodSeparator + methodName, msg,
					tr);
		}
	}

	@SuppressLint("LongLogTag")
	private void TraceUtil()
	{
		if (!_isInstantiationAllowed)
		{
			String errorMessage =
					"You cannot use Constructor to create an instance of this class. " +
							"Instead of it please use TraceUtil.getInstance().";
			Log.e(classSeparator1 + CLASS_NAME + classSeparator2 + methodSeparator + "Constructor",
					errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
	}
}
