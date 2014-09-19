package vace117.creeper.webrtc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import vace117.creeper.logging.CreeperContext;

public class PeerConnectionManager {
	
	/**
	 * Set to true to send an audio track
	 */
	private static final boolean ENABLE_AUDIO = true;
	
	
	
	private static PeerConnectionManager instance;

	private ChannelHandlerContext webSocketContext;
	
	private PeerConnection peerConnection = null;
	private PeerConnectionFactory factory;
	private VideoSource videoSource;
	private PeerConnectionObserverImpl pcObserver;

	public static PeerConnectionManager getInstance(ChannelHandlerContext ctx) {
		if (instance == null) {
			instance = new PeerConnectionManager(ctx);
		}

		return instance;
	}

	public static PeerConnectionManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException(
					"PeerConnectionManager is not yet initialized!");
		}

		return instance;
	}
	
	public static boolean isPeerConnectionManagerAvailable() {
		return instance != null;
	}

	private PeerConnectionManager(ChannelHandlerContext webSocketContext) {
		this.webSocketContext = webSocketContext;
	}
	
	/**
	 * Creates a PeerConnection and adds a Video Track of the webcam to it 
	 */
	public void initPeerConnection(){
		CreeperContext.getInstance().info("Creating a PeerConnection...");
		factory = new PeerConnectionFactory();
		pcObserver = new PeerConnectionObserverImpl(webSocketContext);
		peerConnection = factory.createPeerConnection(
				new ArrayList<PeerConnection.IceServer>(),
				new MediaConstraints(), 
				pcObserver);
		CreeperContext.getInstance().info("PeerConnection State: {}", peerConnection.signalingState());

		// Get the video source
		if ( videoSource == null ) {
			CreeperContext.getInstance().info("Obtaining the default VideoSource...");
			videoSource = factory.createVideoSource(VideoCapturer.create(""), new MediaConstraints());
		}
		else {
			CreeperContext.getInstance().info("Re-using VideoSource...");
		}

		// Create a MediaStream with one video track
		CreeperContext.getInstance().info("Creating a MediaStream...");
		MediaStream lMS = factory.createLocalMediaStream("JavaMediaStream");
        VideoTrack videoTrack = factory.createVideoTrack("JavaMediaStream_v0", videoSource);
        videoTrack.addRenderer(new VideoRenderer(new VideoRendererObserverImpl()));
        lMS.addTrack(videoTrack);
        if ( ENABLE_AUDIO ) {
        	lMS.addTrack(factory.createAudioTrack("JavaMediaStream_a0"));
        }
        peerConnection.addStream(lMS, new MediaConstraints());
	}
	
	public void shutDown() {
		CreeperContext.getInstance().info("SHUTDOWN. Cleaning up WebRTC resources...");
		
		destroyPeerConnection();
		
		if ( videoSource != null ) {
			videoSource.dispose();
		}
		
		if ( factory != null ) {
			factory.dispose();
		}
	}
	
	private void destroyPeerConnection() {
		if ( peerConnection != null ) {
			peerConnection.close();
			peerConnection.dispose();
			peerConnection = null;
		}
	}

	public void createOffer() {
		destroyPeerConnection();
		initPeerConnection();
        
        // We don't want to receive anything
		MediaConstraints sdpConstraints = new MediaConstraints();
		sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveAudio", "true"));
		sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
				"OfferToReceiveVideo", "false"));

		CreeperContext.getInstance().info("Creating an Offer...");
		// Get the Offer SDP
		SdpObserverImpl sdpOfferObserver = new SdpObserverImpl();
		peerConnection.createOffer(sdpOfferObserver, sdpConstraints);
		SessionDescription offerSdp = sdpOfferObserver.getSdp();
		
		// Set local SDP, don't care for any callbacks
		CreeperContext.getInstance().info("Setting LocalDescription...");
		peerConnection.setLocalDescription(new SdpObserverImpl(), offerSdp);

		// Serialize Offer and send to the Browser via a WebSocket
		sendOfferToBrowser(offerSdp);
	}

	/**
	 * Called when an SDP Answer arrives via the WebSocket
	 */
	public void setRemoteDescription(SessionDescription answer) {
		CreeperContext.getInstance().info("Setting Remote Description: {}", answer.description);

		peerConnection.setRemoteDescription(new SdpObserverImpl(), answer);
	}

	/**
	 * Called when a remote ICE candidate arrives via the WebSocket
	 */
	public void addRemoteIceCandidate(IceCandidate candidate) {
		CreeperContext.getInstance().info("Adding Remote Ice Candidate: {}", candidate.sdp);
		
		peerConnection.addIceCandidate(candidate);
	}
	
	@SuppressWarnings("unchecked")
	private void sendOfferToBrowser(SessionDescription offerSdp) {
		CreeperContext.getInstance().info("Sending Offer SDP:\n{}\n", offerSdp.description);
		
		JSONObject offerSdpJson = new JSONObject();
		offerSdpJson.put("sdp", offerSdp.description);
		offerSdpJson.put("type", offerSdp.type.canonicalForm());
		
		JSONObject envelope = new JSONObject();
		envelope.put("offer", offerSdpJson);
		
		webSocketContext.channel().writeAndFlush(
				new TextWebSocketFrame(envelope.toString()));
	}
	
	
}
