package yesman.epicfight.api.forgeevent;

import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Cancelable
public class BattleModeSustainableEvent extends Event {
	private final PlayerPatch<?> playerpatch;
	
	public BattleModeSustainableEvent(PlayerPatch<?> playerpatch) {
		this.playerpatch = playerpatch;
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
	}
}