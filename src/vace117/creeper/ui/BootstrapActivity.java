package vace117.creeper.ui;

import org.webrtc.PeerConnectionFactory;

import vace117.creeper.controller.raspberrypi.RaspberryPiController;
import vace117.creeper.logging.CreeperContext;
import vace117.creeper.signaling.usbsocket.UsbSocketServer;
import vace117.creeper.signaling.websocket.WebSocketServer;
import vace117.creeper.ui.ViewPagerAdapter.Tabs;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

/**
 * Main activity that contains 2 paged fragments: Log and Scary Creeper image.
 * 
 * This is also the bootstrap class that kicks off out Web Server
 *
 * @author Val Blant
 */
public class BootstrapActivity extends FragmentActivity {
	private static WebSocketServer webSocketServer;
	private static UsbSocketServer usbSocketServer;

    private ViewPager viewPager;
    private ViewPagerAdapter mAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bootstrap);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(2);
	}
	
	/**
	 * Called by <code>LogFragment</code> when it has been initialized. We need to wait for this
	 * before calling any code that wants device logging
	 */
	void onLogFragmentReady() {
	    CreeperContext.init(this);
	    
	    CreeperContext.getInstance().controller = new RaspberryPiController();
	    
	    

	    // Check that WebRTC stuff is functional
	    pokeWebRTC();

	    // Run the USB Socket server
		new Thread(new Runnable() {
	        public void run() {
	        	try {
	        		usbSocketServer = new UsbSocketServer(BootstrapActivity.this);
	        		usbSocketServer.run();
				} catch (Exception e) {
					CreeperContext.getInstance().error("Badness:", e);
				}
	        }
	    }).start();

	    // Run the WebServer
		new Thread(new Runnable() {
	        public void run() {
	        	try {
	        		webSocketServer = new WebSocketServer(BootstrapActivity.this);
	        		webSocketServer.run();
				} catch (Exception e) {
					CreeperContext.getInstance().error("Badness:", e);
				}
	        }
	    }).start();
	}
	
	public View getLogFragment() {
		return viewPager.getChildAt(Tabs.LOG.index);
	}
	
	/**
	 * Hide the scrolling log and display the scary creeper instead
	 */
	public void onConnectionEstablished() {
		viewPager.setCurrentItem(Tabs.SCARY_CREEPER.index);
	}
	
	/**
	 * Registers the VM with the native Video and Voice Engines
	 */
	private void pokeWebRTC() {
	    CreeperContext.getInstance().info("Initializing WebRTC...");
	    CreeperContext.getInstance().dieUnless(PeerConnectionFactory.initializeAndroidGlobals(this), "Failed to initializeAndroidGlobals!");
	    CreeperContext.getInstance().info("WebRTC seems to be ready to go!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bootstrap, menu);
		return true;
	}

	
}
