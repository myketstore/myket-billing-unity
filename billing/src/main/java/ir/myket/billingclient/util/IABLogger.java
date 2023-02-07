package ir.myket.billingclient.util;

import android.util.Log;

public class IABLogger {
	public static String TAG = "[MyketIAB]";
	public static boolean DEBUG = false;

	public static void logDebug(String log) {
		if (DEBUG)
			Log.i(TAG, log);
	}

	public static void logError(String log) {
		if (DEBUG)
			Log.e(TAG, log);
	}

	public static void logWarn(String log) {
		if (DEBUG)
			Log.w(TAG, log);
	}

	public static void logEntering(String className, String methodName) {
		if (DEBUG)
			Log.i(TAG, className + "." + methodName + "()");
	}

	public static void logEntering(String className, String methodName, Object param) {
		if (DEBUG)
			Log.i(TAG, className + "." + methodName + "( " + param + " )");
	}

	public static void logEntering(String className, String methodName, Object[] params) {
		if (DEBUG) {
			String prefix = "";
			StringBuilder b = new StringBuilder();
			for (Object p : params) {
				b.append(prefix);
				b.append(p);
				prefix = ", ";
			}
			Log.i(TAG, className + "." + methodName + "( " + b.toString() + " )");
		}
	}
}
