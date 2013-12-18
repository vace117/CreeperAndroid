/**
 * This is the class responsible for negotiating a WebRTC media session
 * 
 * @author Val Blant
 */
if (!window.Creeper) { window.Creeper = {}; }

(function(creeper) {
	/**
	 * Sets up all callbacks and waits for an SDP Offer to come in through the WebSocket
	 */
	creeper.VideoManager = function(webSocket) {
		this.webSocket = webSocket;
		
		// The actual media channels sent are controlled on the Java side
		this.sdpConstraints = {
				'mandatory' : {
					'OfferToReceiveAudio' : true,
					'OfferToReceiveVideo' : true
				}
			};
		
		this.peerConnection = new webkitRTCPeerConnection(null);
		
		// Callback for when remote info arrives
		this.peerConnection.onaddstream = creeper.bindThis(this.gotRemoteStream, this);
		this.peerConnection.onicecandidate = creeper.bindThis(this.gotIceCandidate, this);
		
		// Register ourselves as a listener to WebSocket messages
		webSocket.registerMessageListener( creeper.bindThis(this.onWebSocketMessage, this) );
	}
	
	/**
	 * Knows how to process an incoming SDP Offer or incoming ICE candidates.
	 * 
	 * Returns false if the message could not be processed here
	 */
	creeper.VideoManager.prototype.onWebSocketMessage = function(event) {
		var message = JSON.parse(event.data);
		if (message.offer) {
			var sdpOffer = new RTCSessionDescription(message.offer);

			trace("Java Offer: \n" + sdpOffer.sdp);
			this.peerConnection.setRemoteDescription(sdpOffer);
			this.peerConnection.createAnswer(
					creeper.bindThis(this.gotRemoteDescription, this),
					creeper.bindThis(this.onCreateSessionDescriptionError, this),
					this.sdpConstraints);
			
			return true;
		} else if (message.ice) {
			var remoteIceCandidate = new RTCIceCandidate(message.ice);
			trace("Java ICE Candidate: " + remoteIceCandidate.candidate);
			this.peerConnection.addIceCandidate(remoteIceCandidate);

			return true;
		}
		else {
			return false; // This message was not for us
		}
	}
	
	/**
	 * Error occurred while putting together out SDP response
	 */
	creeper.VideoManager.prototype.onCreateSessionDescriptionError = function(error) {
		console.log('Failed to create session description: ' + error.toString());
	}
	
	/**
	 * Callback for when our SDP answer is ready to be sent out
	 */
	creeper.VideoManager.prototype.gotRemoteDescription = function(answer) {
		this.peerConnection.setLocalDescription(answer);
		trace("Browser's Answer: \n" + answer.sdp);

		this.webSocket.send(JSON.stringify({
			'sdpAnswer' : answer
		}));
	}
		
	/**
	 * Callback for when a local ICE candidate is ready to be sent out
	 */
	creeper.VideoManager.prototype.gotIceCandidate = function(event) {
		if (event.candidate) {
			trace("Browsers ICE Candidate: " + event.candidate.candidate);
			this.webSocket.send(JSON.stringify({
				'ice' : event.candidate
			}));
		}
	}
	
	/**
	 * Callback for when the remote stream is ready to be attached to our <video> element
	 */
	creeper.VideoManager.prototype.gotRemoteStream = function(event) {
		var remoteVideo = document.getElementById("remoteVideo");
		remoteVideo.src = URL.createObjectURL(event.stream);
		remoteVideo.height = creeper.videoHeight;
		trace("Received remote stream");
		
		new creeper.CreeperCommandAndControl(this.webSocket);
	}
	
})(window.Creeper);