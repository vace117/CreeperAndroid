package vace117.creeper.signaling;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import vace117.creeper.logging.CreeperContext;
import vace117.creeper.webrtc.PeerConnectionManager;

/**
 * Detects a completed WebSocket handshake and kicks off the WebRTC setup. The WebSocket
 * is used as a signalling channel to exchange SDP messages when setting up PeerConnection. 
 * 
 * @author Val Blant
 */
public class WebSocketConnectionObserver extends WebSocketServerProtocolHandler {
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		
		if ( WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE == evt ) {
			PeerConnectionManager.getInstance(ctx).createOffer();
		}
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		CreeperContext.getInstance().error("Something went wrong during PeerConnection init!", cause);
		
		super.exceptionCaught(ctx, new WebSocketHandshakeException(cause.getMessage()));
	}







	public WebSocketConnectionObserver(String websocketPath, String subprotocols, boolean allowExtensions) {
		super(websocketPath, subprotocols, allowExtensions);
	}

	public WebSocketConnectionObserver(String websocketPath, String subprotocols) {
		super(websocketPath, subprotocols);
	}

	public WebSocketConnectionObserver(String websocketPath) {
		super(websocketPath);
	}

}
