package vace117.creeper.signaling.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.SocketException;
import java.util.Set;

import vace117.creeper.logging.CreeperContext;
import vace117.creeper.signaling.AbstractSocketServer;
import vace117.creeper.webrtc.PeerConnectionManager;
import android.app.Activity;

/**
 * A WebSocket Server that responds to requests at:
 * <pre>
 * http://localhost:8080/websocket
 * </pre>
 *
 * It also acts as a simple static resource web server at:
 * <pre>
 * http://localhost:8080/web
 * </pre>
 */
public class WebSocketServer extends AbstractSocketServer {
	private static final int PORT = 8000; 

    public WebSocketServer(Activity mainActivity) {
		super(PORT, mainActivity);
	}

	@Override
	protected ChannelInitializer<SocketChannel> createChannelInitializer() {
		return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                    new HttpRequestDecoder(),
                    new HttpObjectAggregator(65536),
                    new HttpResponseEncoder(),
                    new HttpStaticFileServerHandler("/web/creeper.html", mainActivity),
                    new WebSocketConnectionObserver("/websocket"),
                    new WebSocketMessageHandler());
            }
        };
	}
	
	

    @Override
	protected void sayHello() throws SocketException {
    	Set<String> activeAddresses = findActiveInterfaces();
    	
    	CreeperContext.getInstance().info("Creeper standing by. Command interface active at:");
        for ( String addr : activeAddresses ) {
        	CreeperContext.getInstance().info("   http://" + addr + ":" + port);
        }
    	
	}
    

	@Override
	public void shutdown() {
		super.shutdown();
		
		shutdownPeerConnectionManager();
	}

	private final void shutdownPeerConnectionManager() {
    	CreeperContext.getInstance().info("Shutting Down...");
    	
    	if ( PeerConnectionManager.isPeerConnectionManagerAvailable() ) {
    		PeerConnectionManager.getInstance().shutDown();
    	}
    }


}
