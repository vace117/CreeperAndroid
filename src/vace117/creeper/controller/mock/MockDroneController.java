package vace117.creeper.controller.mock;

import vace117.creeper.controller.BiDirectionalCreeperController;
import vace117.creeper.controller.command.CreeperCommand;
import vace117.creeper.controller.response.UsbResponse;

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onUsbMessageReceived(UsbResponse msg) {
		// TODO Auto-generated method stub

	}

}
