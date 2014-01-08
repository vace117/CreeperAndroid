package vace117.creeper.controller.response;

/**
 * This message class represents data sent from the Raspberry Pi to the Android.
 * 
 * @author Val Blant
 */
public class UsbResponse {
	public UsbResponseType messageType;
	public String message;

	public UsbResponse(UsbResponseType messageType, String message) {
		this.messageType = messageType;
		this.message = message;
	}

	@Override
	public String toString() {
		return messageType + ": " + message;
	}
	
	
	
}
