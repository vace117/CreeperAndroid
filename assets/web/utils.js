/**
 * Utility and convenience functions
 * 
 * @author Val Blant
 */
if (!window.Creeper) { window.Creeper = {}; }

(function(creeper) {

	/**
	 * Pretty log to the web page
	 */
	creeper.log = function(msg) {
		var logLinesContainer = $(document.getElementById('logLines'));

		var logHeight = logLinesContainer.height();
		
		if ( logHeight >= (creeper.videoHeight - 80) ) {
			logLinesContainer.empty(); // Clear the log
		}
		logLinesContainer.append("<div>".concat(msg, "</div>"));
	}
	
	/**
	 * Ensures that when a function is called, the value of 'this' will be set to 
	 * the passed in thisValue
	 */
	creeper.bindThis = function(func, thisValue) {
		return function() {
			return func.apply(thisValue, arguments);
		}
	}

	

})(window.Creeper);

/**
 * Prints debugging messages to the javascript console 
 */
function trace(text) {
	console.log((performance.now() / 1000).toFixed(3) + ": " + text);
}
