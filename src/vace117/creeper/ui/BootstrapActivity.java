package vace117.creeper.ui;

import vace117.creeper.logging.Logger;
import vace117.creeper.signaling.WebSocketServer;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;

public class BootstrapActivity extends Activity {
	
	private static final int PORT = 8000; 
	private static final WebSocketServer webSocketServer = new WebSocketServer(PORT);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bootstrap);
		
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
		new Thread(new Runnable() {
	        public void run() {
	        	try {
	        		Logger.info("Threaded 1");
	        		webSocketServer.run();
				} catch (Exception e) {
					Logger.error("Badness:", e);
				}
	        }
	    }).start();
	}

	
	// TODO: This might never be called, so figure out where to release resources!
	@Override
	protected void onDestroy() {
		super.onDestroy();

		webSocketServer.shutdown(); 
	}
	
//	  @Override
//	  public void onPause() {
//	    super.onPause();
//	    vsv.onPause();
//	    if (videoSource != null) {
//	      videoSource.stop();
//	    }
//	  }
//
//	  @Override
//	  public void onResume() {
//	    super.onResume();
//	    vsv.onResume();
//	    if (videoSource != null) {
//	      videoSource.restart();
//	    }
//	  }
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bootstrap, menu);
		return true;
	}

	
}
