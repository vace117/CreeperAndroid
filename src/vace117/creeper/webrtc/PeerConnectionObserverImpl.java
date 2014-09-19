package vace117.creeper.webrtc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.json.simple.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.SignalingState;

import vace117.creeper.logging.CreeperContext;

/**
 * Sends locally detected ICE candidates to the browser via the WebSocket, as well as handling some logging of connection states.
 * 
 * @author Val Blant
 */
public class PeerConnectionObserverImpl implements PeerConnection.Observer {
	private ChannelHandlerContext webSocketContext;
	
	
	public PeerConnectionObserverImpl(ChannelHandlerContext webSocketContext) {
		this.webSocketContext = webSocketContext;
	}

	/**
	 * Called when we need to share a ICE candidate
	 */
	@Override
	public void onIceCandidate(IceCandidate candidate) {
		sendIceCandidateToBrowser(candidate);
	}

	@SuppressWarnings("unchecked")
	private void sendIceCandidateToBrowser(IceCandidate candidate) {
		CreeperContext.getInstance().info("Sending ICE Candidate: {}", candidate.sdp);
		
		JSONObject iceCandidateJson = new JSONObject();
		iceCandidateJson.put("id", candidate.sdpMid);
		iceCandidateJson.put("label", candidate.sdpMLineIndex);
		iceCandidateJson.put("candidate", candidate.sdp);
		
		JSONObject envelope = new JSONObject();
		envelope.put("ice", iceCandidateJson);
		
		webSocketContext.channel().writeAndFlush(
				new TextWebSocketFrame(envelope.toString()));
	}
	
	
	@Override
	public void onError() {
		throw new RuntimeException("PeerConnection error!");
	}

	/**
	 * This is called when a stream from the browser has been added to our PeerConnection
	 */
	@Override
	public void onAddStream(MediaStream stream) {
		CreeperContext.getInstance().info("Received a MediaStream from the browser: {}", stream);
	}


	
	
	
	
	@Override
	public void onSignalingChange(SignalingState newState) {
		CreeperContext.getInstance().info("ICE SignalingState: {}", newState);
	}

	@Override
	public void onIceConnectionChange(IceConnectionState newState) {
		CreeperContext.getInstance().info("ICE ConnectionState: {}", newState);
		if ( IceConnectionState.CONNECTED.equals(newState) ) {
			// Hide the log and put up the picture
			CreeperContext.getInstance().mainActivity.onConnectionEstablished();
		}
	}

	@Override
	public void onIceGatheringChange(IceGatheringState newState) {
		CreeperContext.getInstance().info("ICE GatheringState: {}", newState);
	}

	@Override
	public void onRemoveStream(MediaStream stream) {
	}

	@Override
	public void onDataChannel(DataChannel dataChannel) {
	}

}
