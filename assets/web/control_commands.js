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
				67: "LOOK_CENTER"   // c 
		};
		
		// Register key handler functions
		//
		this.held_down_keys = {}; // Map of currently held down keys
		$(document).keydown( creeper.bindThis(this.keyDownHandler, this) );
		$(document).keyup( creeper.bindThis(this.keyUpHandler, this) );
		trace("Registered key press listener.");
		
		// Register ourselves as a listener to WebSocket messages
		webSocket.registerMessageListener( creeper.bindThis(this.onWebSocketMessage, this) );
		trace("Registered WebSocket status message processor.");
	}
	
	/**
	 * Key Down handler. Adds the pressed down key to the list and triggers command transmission
	 */
	creeper.CreeperCommandAndControl.prototype.keyDownHandler = function(e) {
		for (var keyCode in this.COMMANDS ) {
			if ( e.which == keyCode ) {
				e.preventDefault();
				this.held_down_keys[e.which] = true;
				this.sendCommands();
				return;
			}
		}
	}

	/**
	 * Key Up handler. Deletes the released key from the list and triggers command transmission
	 */
	creeper.CreeperCommandAndControl.prototype.keyUpHandler = function(e) {
		for (var keyCode in this.COMMANDS ) {
			if ( e.which == keyCode ) {
				e.preventDefault();
			    delete this.held_down_keys[e.which];
				this.sendCommands();
				return;
			}
		}
	}

	/**
	 * Transmits the selected command over the websocket
	 */
	creeper.CreeperCommandAndControl.prototype.sendCommands = function() {
		var commandStrings = "";
		for (var keyCode in this.held_down_keys ) {
			var command = this.COMMANDS[keyCode];
			commandStrings += command + ", "; 
			
			this.webSocket.send(JSON.stringify({
				'command' : command
			}));
		}
		
		if ( commandStrings.length > 0 ) {
			trace("Browser Says: " + commandStrings);
		}
		

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