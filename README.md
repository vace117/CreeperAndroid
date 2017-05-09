CreeperAndroid
==============

This is a WebRTC android app for communicating with the Creeper Drone. 

Creeper Drone is a hobby project which integrates an Android device onto a remote controlled car platform in order to provide drive-by-wire capability from a browser.

This Android app starts a web server on port 8000 and awaits a connection from a browser. Once the browser hits the port, an HTML file and some javascript is served to the browser, while the Android app initiates local WebRTC setup. 

Once Android is ready to send an SDP offer, it is forwarded to the browser via a WebSocket on which the page is listening. The served javascript code handles local WebRTC setup inside the browser, and uses the WebSocket to send the SDP answer.

For more details about the design and architecture, please refer to this Instructible: http://www.instructables.com/id/WebRTC-Creeper-Drone-Browser-Controlled-RC-Car/

