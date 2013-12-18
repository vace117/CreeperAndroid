package vace117.creeper.controller.mock;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.json.simple.JSONObject;

import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.controller.command.CreeperCommand;
import vace117.creeper.controller.response.UsbResponse;
import vace117.creeper.controller.response.UsbResponseType;
import vace117.creeper.logging.CreeperContext;

/**
 * This controller can be used when we are not hooked up to the Rasberry Pi.
 * 
 * It will simply echo the incoming commands back to the browser as status messages.
 *
 * @author Val Blant
 */
public class MockDroneController implements BiDirectionalCreeperController {

	@Override
	public void dispatchToPi(CreeperCommand msg) {
		onUsbMessageReceived(new UsbResponse(UsbResponseType.STATUS_MSG, msg.commandType.name()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUsbMessageReceived(UsbResponse msg) {
		CreeperContext.getInstance().info_console("Sending Status message to the browser: {}", msg.message);
		
		JSONObject envelope = new JSONObject();
		envelope.put("statusMsg", msg.message);
		
		CreeperContext.getInstance().webSocketContext.channel().writeAndFlush(
				new TextWebSocketFrame(envelope.toString()));
		
	}

}
