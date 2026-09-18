package yesman.epicfight.forgecompat.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class MovementInputUpdateEvent extends Event {
	private final Player entity;
	private final Input input;

	public MovementInputUpdateEvent(Player entity, Input input) {
		this.entity = entity;
		this.input = input;
	}

	public Player getEntity() {
		return this.entity;
	}

	public Input getInput() {
		return this.input;
	}
}
