package yesman.epicfight.forgecompat.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterCommandsEvent extends Event {
	private final CommandDispatcher<CommandSourceStack> dispatcher;
	private final CommandBuildContext buildContext;
	private final Commands.CommandSelection commandSelection;

	public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection commandSelection) {
		this.dispatcher = dispatcher;
		this.buildContext = buildContext;
		this.commandSelection = commandSelection;
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return this.dispatcher;
	}

	public CommandBuildContext getBuildContext() {
		return this.buildContext;
	}

	public Commands.CommandSelection getCommandSelection() {
		return this.commandSelection;
	}
}
