package vace117.creeper.controller.command;

/**
 * List all of the possible Creeper commands that can arrive from the browser, along with their
 * range of values (i.e. resolution) 
 *
 * @author Val Blant
 */
public enum CreeperCommandType {
	ACCELERATE(10),
	STOP(0),
	WHEELS_LEFT(10),
	WHEELS_RIGHT(10),
	WHEELS_STRAIGHT(0),
	REVERSE_ACCELERATE(10),
	
	LOOK_LEFT(90),
	LOOK_RIGHT(90),
	LOOK_UP(90),
	LOOK_DOWN(90),
	LOOK_CENTER(0);
	
	/**
	 * This is the range of values the function that this command controls can take on.
	 */
	private final int commandRange;
	
	
	private CreeperCommandType(int commandRange) {
		this.commandRange = commandRange;
	}

	public static CreeperCommandType getCommand(String fromBrowser) {
		for ( CreeperCommandType command : values() ) {
			if ( command.name().equalsIgnoreCase(fromBrowser) ) {
				return command;
			}
		}
		
		throw new IllegalArgumentException("Unknown command from browser!");
	}

	public int getCommandRange() {
		return commandRange;
	}
	
	
}
