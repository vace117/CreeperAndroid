package vace117.creeper.controller.response;

public enum UsbResponseType {
	CREEPER_READY, 
	STATUS_MSG;
	
	public static UsbResponseType getResponseType(String fromUsbSocket) {
		for ( UsbResponseType responseType : values() ) {
			if ( responseType.name().equalsIgnoreCase(fromUsbSocket) ) {
				return responseType;
			}
		}
		
		throw new IllegalArgumentException("Unknown USB Response message type!");
	}
	
}
