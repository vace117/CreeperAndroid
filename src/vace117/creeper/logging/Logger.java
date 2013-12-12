package vace117.creeper.logging;

import android.util.Log;

public class Logger {
	public static final String TAG = "Creeper1";
			
	public static synchronized void error(String msg, Throwable e) {
		Log.e(TAG, msg, e);
	}

	public static synchronized void error(Throwable e) {
		Log.e(TAG, "Exception:", e);
	}

	public static synchronized void info(String msg) {
		Log.i(TAG, doReplacements(msg, new Object[] {}));
	}

	public static synchronized void info(String msg, Object... replacementArgs) {
		Log.i(TAG, doReplacements(msg, replacementArgs));
	}

	public static synchronized void warn(String msg, Object... replacementArgs) {
		Log.w(TAG, doReplacements(msg, replacementArgs));
	}

	private static String doReplacements(String msg, Object... replacementArgs) {
		String processedString = msg;
		
		if ( replacementArgs != null ) {
			for ( Object arg : replacementArgs ) {
				processedString = processedString.replaceFirst("\\{\\}", arg.toString());
			}
		}
		
		return processedString;
	}

}
