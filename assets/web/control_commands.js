/**
 * This class is responsible for collecting keypresses, converting them to Creeper commands
 * and sending them over the WebSocket.
 * 
 * @author Val Blant
 */


if (!window.Creeper) { window.Creeper = {}; }

(function(creeper) {
	/**
	 * Attaches a handler to document.keydown
	 */
	creeper.CreeperCommandAndControl = function(webSocket) {
		this.webSocket = webSocket;
		
		/**
		 * These are the known commands
		 */
		this.COMMANDS = {
				65: "LOOK_LEFT",	// a 
				68: "LOOK_RIGHT", 	// d
				87: "LOOK_DOWN", 	// w
				83: "LOOK_UP", 		// s
				0: "LOOK_CENTER" 
		};
		
		$(document).keydown( creeper.bindThis(this.keyHandler, this) );
		trace("Registered key press listener.");
		
		// Register ourselves as a listener to WebSocket messages
		webSocket.registerMessageListener( creeper.bindThis(this.onWebSocketMessage, this) );
		trace("Registered WebSocket status message processor.");
	}
	
	/**
	 * Key press handler. Dispatches commands based on the COMMANDS map.
	 */
	creeper.CreeperCommandAndControl.prototype.keyHandler = function(e) {
		for (var keyCode in this.COMMANDS ) {
			if ( e.which == keyCode ) {
				e.preventDefault();
				this.sendCommand(this.COMMANDS[keyCode]);
				return;
			}
		}
	}
	
	/**
	 * Transmits the selected command over the websocket
	 */
	creeper.CreeperCommandAndControl.prototype.sendCommand = function(command) {
		trace("Browser Says: " + command);
		
		this.webSocket.send(JSON.stringify({
			'command' : command
		}));
	}
	
	/**
	 * Knows how to process status responses from the Creeper
	 * 
	 * Returns false if the message could not be processed here
	 */
	creeper.CreeperCommandAndControl.prototype.onWebSocketMessage = function(event) {
		var message = JSON.parse(event.data);
		if (message.statusMsg) {
			
			trace("Creeper Says: " + message.statusMsg);
			creeper.log("CREEPER SAYS: " + message.statusMsg);
			
			return true;
		}
		else {
			return false; // This message was not for us
		}
	}
	
	
})(window.Creeper);