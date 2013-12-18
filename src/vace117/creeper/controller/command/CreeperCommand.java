package vace117.creeper.controller.command;

public class CreeperCommand {
	public CreeperCommandType commandType;
	public int setting = 0;

	public CreeperCommand(CreeperCommandType commandType, int setting) {
		this(commandType);
		this.setting = setting;
	}
	
	public CreeperCommand(CreeperCommandType commandType) {
		this.commandType = commandType;
	}

	@Override
	public String toString() {
		return commandType.name() + ":" + setting;
	}
	
	
}
