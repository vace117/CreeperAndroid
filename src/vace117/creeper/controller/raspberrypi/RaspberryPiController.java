package vace117.creeper.controller.raspberrypi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.json.simple.JSONObject;

import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.controller.command.CreeperCommand;
import vace117.creeper.controller.response.UsbResponse;
import vace117.creeper.controller.response.UsbResponseType;
import vace117.creeper.logging.CreeperContext;

/**
 * The central command dispatch. Commands will arrive via the WebSocket, get processed
 * and finally dispatched to the Raspberry Pi via a USB network socket.
 *
 * @author Val Blant
 */
public class RaspberryPiController implements BiDirectionalCreeperController {
	
	@Override
	public void dispatchToPi(CreeperCommand msg) {
		ChannelHandlerContext ctx = CreeperContext.getInstance().usbSocketContext;
		if ( ctx != null ) {
			ctx.channel().writeAndFlush(msg);
		}
		else {
			CreeperContext.getInstance().info("Cannot send command to Raspberry Pi, b/c we have not yet received a CREEPER_READY message.");
		}
	}

	@Override
	public void onUsbMessageReceived(UsbResponse msg) {
		CreeperContext.getInstance().sendStatusMessageToBrowser(msg.toString());

		if ( UsbResponseType.CREEPER_READY.equals(msg.messageType) ) {
			CreeperContext.getInstance().info_console("Raspberry Pi is Ready!");
		}
		
	}

}
