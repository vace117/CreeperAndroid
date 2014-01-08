package vace117.creeper.controller.command;

public class CreeperCommand {
	public CreeperCommandType commandType;
	
	public CreeperCommand(CreeperCommandType commandType) {
		this.commandType = commandType;
	}

	@Override
	public String toString() {
		return commandType.name();
	}
	
	
}
