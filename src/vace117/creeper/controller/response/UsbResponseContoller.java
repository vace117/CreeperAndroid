package vace117.creeper.controller.response;

/**
 * Interface used by controllers that want to handle USB response messages from Raspberry Pi
 *
 * @author Val Blant
 */
public interface UsbResponseContoller {
	public void onUsbMessageReceived(UsbResponse msg);
}
