package vace117.creeper.ui;

import org.webrtc.PeerConnectionFactory;

import vace117.creeper.logging.CreeperContext;
import vace117.creeper.signaling.WebSocketServer;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class BootstrapActivity extends Activity {
	
	private static final int PORT = 8000; 
	private static WebSocketServer webSocketServer;
	public TextView logString;
	private ImageView creeperImgView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bootstrap);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    logString = (TextView) findViewById(R.id.logString);
	    logString.setMovementMethod(new ScrollingMovementMethod());
	    
	    creeperImgView = (ImageView) findViewById(R.id.creeperImg);
	    creeperImgView.setVisibility(View.INVISIBLE);
	    
	    CreeperContext.init(this);


		// Check that WebRTC stuff is functional
	    pokeWebRTC();

	    // Run the WebServer
		new Thread(new Runnable() {
	        public void run() {
	        	try {
	        		webSocketServer = new WebSocketServer(PORT, BootstrapActivity.this);
	        		webSocketServer.run();
				} catch (Exception e) {
					CreeperContext.getInstance().error("Badness:", e);
				}
	        }
	    }).start();
	}
	
	/**
	 * Hide the scrolling log and display the scary creeper instead
	 */
	public void onConnectionEstablished() {
		logString.post(new Runnable() {
			public void run() {
				logString.setVisibility(View.INVISIBLE);
				creeperImgView.setVisibility(View.VISIBLE);
			}
		});
	}
	
	/**
	 * Registers the VM with the native Video and Voice Engines
	 */
	private void pokeWebRTC() {
	    CreeperContext.getInstance().info("Initializing WebRTC...");
	    CreeperContext.getInstance().dieUnless(PeerConnectionFactory.initializeAndroidGlobals(this), "Failed to initializeAndroidGlobals!");
/*	    PeerConnectionFactory factory = new PeerConnectionFactory();
	    factory.dispose();
*/	    CreeperContext.getInstance().info("WebRTC seems to be ready to go!");
	}

	
//	// TODO: This might never be called, so figure out where to release resources!
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//
//		webSocketServer.shutdown(); 
//	}
	
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
