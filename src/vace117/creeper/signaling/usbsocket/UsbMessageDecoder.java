package vace117.creeper.signaling.usbsocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.controller.response.UsbResponse;
import vace117.creeper.controller.response.UsbResponseType;
import vace117.creeper.logging.CreeperContext;

/**
 * Receives the message that came from Raspberry Pi over USB as a String, and
 * parses it into a <code>UsbResponse</code>. If parsing is successful, the message
 * is forwarded to the controller for processing.
 *
 * @author Val Blant
 */
public class UsbMessageDecoder extends MessageToMessageDecoder<String> {
	
	private BiDirectionalCreeperController controller;
	
	public UsbMessageDecoder(BiDirectionalCreeperController controller) {
		this.controller = controller;
	}


	/**
	 * The expected message format is "UsbResponseType:message"
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		int delimPosition = msg.indexOf(':');
		String msgTypeStr = msg.substring(0, delimPosition);
		String msgText = msg.substring(delimPosition + 1);
		
		try {
			UsbResponse usbResponse = new UsbResponse(UsbResponseType.getResponseType(msgTypeStr), msgText);
			controller.onUsbMessageReceived(usbResponse);
		} catch (IllegalArgumentException e) {
			CreeperContext.getInstance().error("Unknown USB Response type received over USB Socket! MESSAGE: '" + msg + "'", e);
		}
		
	}


}
