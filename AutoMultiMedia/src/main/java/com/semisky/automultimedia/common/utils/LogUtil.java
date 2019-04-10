package com.semisky.automultimedia.common.utils;

import android.util.Log;


public class LogUtil {
	private static final String PREFIX_LOG = "cx62_";
	private static String TAG = "xj_AutoMultiMedia->";
	private static boolean DEBUG = true;

	public static String makeLogTag(Class clz){
		return  PREFIX_LOG+clz.getSimpleName();
	}

	// VERBOSE
	public static void v(String tag, String msg) {

		if (DEBUG) {
			Log.v(TAG + tag, msg);
		}
	}
	// DEBUG
	public static void d(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(TAG + tag, msg);
		}
	}

	public static void i(String msg) {
		if (DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DEBUG) {
			Log.i(TAG + tag, msg);
		}
	}

	public static void w(String msg) {
		if (DEBUG) {
			Log.w(TAG, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG) {
			Log.w(TAG + tag, msg);
		}
	}

	public static void e(String msg) {
		if (DEBUG) {
			Log.e(TAG, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG) {
			Log.e(TAG + tag, msg);
		}
	}

}
