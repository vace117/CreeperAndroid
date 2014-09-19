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
		
		this.peerConnection = new webkitRTCPeerConnection(null);
		
		// Callbacks for when remote info arrives
		this.peerConnection.onaddstream = creeper.bindThis(this.gotRemoteStream, this);
		this.peerConnection.onicecandidate = creeper.bindThis(this.gotIceCandidate, this);
		
		
		// Register ourselves as a listener to WebSocket messages
		webSocket.registerMessageListener( creeper.bindThis(this.onWebSocketMessage, this) );
	}
	
	/**
	 * Callback for when we have obtained the browser's media stream
	 */
	creeper.VideoManager.prototype.onMediaSuccess = function(mediaStream) {
		this.peerConnection.addStream(mediaStream);
		trace("Added browser's media stream to peerConnection.");

		// Create an SDP Answer
		this.peerConnection.createAnswer(
				creeper.bindThis(this.gotRemoteDescription, this),
				creeper.bindThis(this.onCreateSessionDescriptionError, this),
				this.sdpConstraints);			
	}

	/**
	 * Error calllback used when there was a problem obtaining browser's media stream
	 */
	creeper.VideoManager.prototype.onMediaError = function(error) {
		trace("Could not get media stream from the browser: " + error.toString());
	}

	/**
	 * Knows how to process an incoming SDP Offer or incoming ICE candidates.
	 * 
	 * Returns false if the message could not be processed here
	 */
	creeper.VideoManager.prototype.onWebSocketMessage = function(event) {
		var message = JSON.parse(event.data);
		if (message.offer) {
			
			// Get the browser's audio stream from a microphone 
			navigator.webkitGetUserMedia(
					{video:false, audio:true}, 
					creeper.bindThis(this.onMediaSuccess, this), 
					creeper.bindThis(this.onMediaError, this));
			
			var sdpOffer = new RTCSessionDescription(message.offer);

			trace("Java Offer: \n" + sdpOffer.sdp);
			this.peerConnection.setRemoteDescription(sdpOffer);

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
		answer.sdp = this.forceISACAudioCodec(answer.sdp);
		trace("Browser's Answer: \n" + answer.sdp);
		this.peerConnection.setLocalDescription(answer);

		this.webSocket.send(JSON.stringify({
			'sdpAnswer' : answer
		}));
	}
	
	/**
	 * It is necessary to modify our SDP answer in order to ensure that ISAC audio codec is used,
	 * instead of the default Opus codec, which requires 48kHz audio. Ansroid can only provide 16kHz audio, 
	 * so we must remove Opus from the list of options and select the next best thing, which is ISAC/16000.  
	 */
	creeper.VideoManager.prototype.forceISACAudioCodec = function(sdp) {
		var myRegexp = /a=rtpmap:(.*?) ISAC\/16000/g; // Get the id for ISAC codec
		var matches = myRegexp.exec(sdp);
		
		if ( matches === null ) {
			trace("ERROR! No ISAC/16000 audio codec available. You have to pick something else, since Opus doesn't work!");
		}
 		else {
 			var isacId = matches[1];
			var sdpLines = sdp.split('\r\n');

			myRegexp = /a=rtpmap:(.*?) opus\/48000/g; // Get the id for opus
			matches = myRegexp.exec(sdp);
			
			if ( matches !== null ) {
				var opusId = matches[1];
			}

			for ( var i = 0; i < sdpLines.length; i++) {
				if (sdpLines[i].search('m=audio') !== -1) {
					var tokens = sdpLines[i].split(' ');
					// Change the audio line to specify only the ISAC id
					sdpLines[i] = tokens[0].concat(" ", tokens[1], " ", tokens[2], " ", isacId);
				}
				
				// Remove all lines that reference opus
				if ( opusId !== null && sdpLines[i].search(":".concat(opusId)) !== -1 ) {
					sdpLines.splice(i--, 1); // Delete array element and adjust iteration index
				}
			}
		}
		
		sdp = sdpLines.join('\r\n');
		return sdp;
	};
	
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