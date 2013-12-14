package vace117.creeper.logging;

import vace117.creeper.ui.BootstrapActivity;
import android.util.Log;

public class CreeperContext {
	public final String TAG = "Creeper1";
	public BootstrapActivity mainActivity;
	private static CreeperContext instance;
	
	
	
	private CreeperContext(BootstrapActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public static CreeperContext init(BootstrapActivity mainActivity) {
		instance = new CreeperContext(mainActivity);
		return getInstance();
	}
	
	public static CreeperContext getInstance() {
		return instance;
	}

	public synchronized void error(String msg, Throwable e) {
		Log.e(TAG, msg, e);
		updateLogString(msg);
	}

	public synchronized void error(Throwable e) {
		Log.e(TAG, "Exception:", e);
	}

	public synchronized void info(String msg) {
		Log.i(TAG, doReplacements(msg, new Object[] {}));
		updateLogString(msg);
	}

	public synchronized void info(String msg, Object... replacementArgs) {
		String m = doReplacements(msg, replacementArgs);
		Log.i(TAG, m);
		updateLogString(m);
	}

	public synchronized void warn(String msg, Object... replacementArgs) {
		String m = doReplacements(msg, replacementArgs);
		Log.w(TAG, m);
		updateLogString(m);
	}

	private String doReplacements(String msg, Object... replacementArgs) {
		String processedString = msg;

		if (replacementArgs != null) {
			for (Object arg : replacementArgs) {
				processedString = processedString.replaceFirst("\\{\\}", arg.toString());
			}
		}

		return processedString;
	}

	public synchronized void dieUnless(boolean condition, String msg) {
		if (!condition) {
			RuntimeException ex = new RuntimeException(msg);
			CreeperContext.getInstance().error("I am dyiiiing!", ex);
			throw ex;
		}
	}

	public void updateLogString(final String msg) {
		mainActivity.logString.post(new Runnable() {
			@Override
			public void run() {
				mainActivity.logString.setText(mainActivity.logString.getText() + "\n" + msg);
			}
		});
		
	}
}
