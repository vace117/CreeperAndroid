package vace117.creeper.controller.raspberrypi;

import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.controller.command.CreeperCommand;
import vace117.creeper.controller.response.UsbResponse;

/**
 * The central command dispatch. Commands will arrive via the WebSocket, get processed
 * and finally dispatched to the Raspberry Pi via a USB network socket.
 *
 * @author Val Blant
 */
public class RaspberryPiController implements BiDirectionalCreeperController {

	@Override
	public void dispatchToPi(CreeperCommand msg) {
		
	}

	@Override
	public void onUsbMessageReceived(UsbResponse msg) {
		// TODO Auto-generated method stub
		
	}

}
