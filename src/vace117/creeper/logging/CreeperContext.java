package vace117.creeper.logging;

import org.json.simple.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.ui.BootstrapActivity;
import vace117.creeper.ui.R;
import android.util.Log;
import android.widget.TextView;

/**
 * Context, logging, utilities, etc.
 *
 * @author Val Blant
 */
public class CreeperContext {
	public final String TAG = "Creeper1";
	public BootstrapActivity mainActivity;
	public BiDirectionalCreeperController controller;
	public ChannelHandlerContext webSocketContext;
	public ChannelHandlerContext usbSocketContext;
	
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

	public void error(String msg) {
		Log.e(TAG, msg);
		updateLogString(msg);
	}

	public void error(String msg, Throwable e) {
		Log.e(TAG, msg, e);
		updateLogString(msg);
	}

	public void error(Throwable e) {
		Log.e(TAG, "Exception:", e);
	}

	public void info(String msg) {
		Log.i(TAG, doReplacements(msg, new Object[] {}));
		updateLogString(msg);
	}

	public void info(String msg, Object... replacementArgs) {
		String m = doReplacements(msg, replacementArgs);
		Log.i(TAG, m);
		updateLogString(m);
	}
	
	public void info_console(String msg, Object... replacementArgs) {
		String m = doReplacements(msg, replacementArgs);
		Log.i(TAG, m);
	}


	public void warn(String msg, Object... replacementArgs) {
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

	public void dieUnless(boolean condition, String msg) {
		if (!condition) {
			RuntimeException ex = new RuntimeException(msg);
			CreeperContext.getInstance().error("I am dyiiiing!", ex);
			throw ex;
		}
	}

	public void updateLogString(final String msg) {
		final TextView logString = (TextView) mainActivity.getLogFragment().findViewById(R.id.logString); 
		logString.post(new Runnable() {
			@Override
			public void run() {
				logString.setText(logString.getText() + "\n" + msg);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void sendStatusMessageToBrowser(String message) {
		JSONObject envelope = new JSONObject();
		envelope.put("statusMsg", message);
		
		webSocketContext.channel().writeAndFlush( new TextWebSocketFrame(envelope.toString()) );
	}
}
