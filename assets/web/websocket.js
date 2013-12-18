/**
 * This is the class responsible for establishing a WebSocket connection to the server
 * and dispatching incoming socket messages to registered listeners.  
 * 
 * @author Val Blant
 */
if (!window.Creeper) { window.Creeper = {}; }

(function(creeper) {
	
	/**
	 * Opens a connection to the WebSocket server
	 */
	creeper.CreeperSocket = function() {
		var socket;
		if (!window.WebSocket) {
			window.WebSocket = window.MozWebSocket;
		}

		if (window.WebSocket) {
			socket = new WebSocket("ws://".concat(location.host,"/websocket"));
			socket.onopen = this.onopen;
			socket.onmessage = creeper.bindThis(this.onmessage, this);
			socket.onclose = this.onclose;
		} else {
			alert("Your browser does not support Web Sockets!");
		}
		
		this.send = function(msg) {
			socket.send(msg);
		}
		
	}
	
	/**
	 * Called by clients to register listeners that should be notified of incoming messages
	 */
	creeper.CreeperSocket.prototype.registerMessageListener = function(listener) {
		if ( !this.messageHandlers ) {
			this.messageHandlers = [];
		}
		
		this.messageHandlers.push(listener);
	}
	
	/**
	 * Calls all listeners, until one of the returns 'true', meaning that message was processed 
	 */
	creeper.CreeperSocket.prototype.onmessage = function(event) {
		if ( this.messageHandlers ) {
			for (var i = 0; i < this.messageHandlers.length; i++) {
				var success = this.messageHandlers[i].call(this, event);
				if (success) break;
			}			
		}
	}

	/**
	 * Socket open handler
	 */
	creeper.CreeperSocket.prototype.onopen = function() {
		creeper.log("Web Socket opened.");
	}

	/**
	 * Socket close handler
	 */
	creeper.CreeperSocket.prototype.onclose = function() {
		creeper.log("Web Socket closed.");
	}

})(window.Creeper);