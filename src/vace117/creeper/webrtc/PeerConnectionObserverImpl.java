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

import vace117.creeper.logging.Logger;

/**
 * Sends locally detected ICE candidates to the browser via the WebSocket
 * 
 * @author Val Blant
 */
public class PeerConnectionObserverImpl implements PeerConnection.Observer {
	private ChannelHandlerContext webSocketContext;
	
	
	public PeerConnectionObserverImpl(ChannelHandlerContext webSocketContext) {
		this.webSocketContext = webSocketContext;
	}

	@Override
	public void onIceCandidate(IceCandidate candidate) {
		sendIceCandidateToBrowser(candidate);
	}

	@SuppressWarnings("unchecked")
	private void sendIceCandidateToBrowser(IceCandidate candidate) {
		Logger.info("Sending ICE Candidate: {}", candidate.sdp);
		
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

	@Override
	public void onAddStream(MediaStream stream) {
		throw new UnsupportedOperationException("We should not be adding a remote stream on the local PeerConnection!");
	}


	
	
	
	
	@Override
	public void onSignalingChange(SignalingState newState) {
		Logger.info("ICE SignalingState: {}", newState);
	}

	@Override
	public void onIceConnectionChange(IceConnectionState newState) {
		Logger.info("ICE ConnectionState: {}", newState);
	}

	@Override
	public void onIceGatheringChange(IceGatheringState newState) {
		Logger.info("ICE GatheringState: {}", newState);
	}

	@Override
	public void onRemoveStream(MediaStream stream) {
	}

	@Override
	public void onDataChannel(DataChannel dataChannel) {
	}

}
