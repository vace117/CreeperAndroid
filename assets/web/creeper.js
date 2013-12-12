$(document).ready(function() {
	var sdpConstraints = {
		'mandatory' : {
			'OfferToReceiveAudio' : true,
			'OfferToReceiveVideo' : true
		}
	};

	var peerConnection = new webkitRTCPeerConnection(null);
	peerConnection.onaddstream = gotRemoteStream;
	peerConnection.onicecandidate = gotIceCandidate;

	var socket;
	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}

	if (window.WebSocket) {
		socket = new WebSocket("ws://".concat(location.host,"/websocket"));
		socket.onopen = onopen;
		socket.onmessage = onmessage;
		socket.onclose = onclose;
	} else {
		alert("Your browser does not support Web Socket.");
	}

	function onopen(event) {
		getStatusElement().textContent = "Web Socket opened!";
	}

	function onmessage(event) {
		var message = JSON.parse(event.data);
		if (message.offer) {
			var sdpOffer = new RTCSessionDescription(
					message.offer);

			trace("Java Offer: \n" + sdpOffer.sdp);
			peerConnection.setRemoteDescription(sdpOffer);
			peerConnection.createAnswer(gotRemoteDescription,
					onCreateSessionDescriptionError,
					sdpConstraints);
		} else if (message.ice) {
			var remoteIceCandidate = new RTCIceCandidate(
					message.ice);
			trace("Java ICE Candidate: "
					+ remoteIceCandidate.candidate);
			peerConnection.addIceCandidate(remoteIceCandidate);
		}
	}

	function onCreateSessionDescriptionError(error) {
		console.log('Failed to create session description: '
				+ error.toString());
	}
	function gotRemoteDescription(answer) {
		peerConnection.setLocalDescription(answer);
		trace("Browser's Answer: \n" + answer.sdp);

		socket.send(JSON.stringify({
			'sdpAnswer' : answer
		}));
	}
	function gotIceCandidate(event) {
		if (event.candidate) {
			trace("Browsers ICE Candidate: "
					+ event.candidate.candidate);
			socket.send(JSON.stringify({
				'ice' : event.candidate
			}));
		}
	}
	function gotRemoteStream(event) {
		var remoteVideo = document
				.getElementById("remoteVideo");
		remoteVideo.src = URL.createObjectURL(event.stream);
		remoteVideo.height = window.innerHeight - 80;
		trace("Received remote stream");
	}

	function onclose(event) {
		getStatusElement().textContent = "Web Socket closed";
	}

	function getStatusElement() {
		return document.getElementById('responseText');
	}

	function trace(text) {
		console.log((performance.now() / 1000).toFixed(3)
				+ ": " + text);
	}
});