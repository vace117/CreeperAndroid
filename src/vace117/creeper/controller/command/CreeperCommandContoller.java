package vace117.creeper.controller.command;

/**
 * Interface used by controllers that want to handle commands from the browser and send them over to Raspberry Pi
 *
 * @author Val Blant
 */
public interface CreeperCommandContoller {
	public void dispatchToPi(CreeperCommand msg);
}
