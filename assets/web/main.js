/**
 * Main entry point
 * 
 * @author Val Blant
 */
$(document).ready(function() {
	
(function(creeper) {
	
	creeper.videoHeight = window.innerHeight - 80;
		
	var socket = new creeper.CreeperSocket()
	var videoManager = new creeper.VideoManager(socket);
		
})(window.Creeper);

});