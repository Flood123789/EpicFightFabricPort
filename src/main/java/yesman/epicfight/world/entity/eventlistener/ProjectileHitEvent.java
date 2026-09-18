package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.forgecompat.event.entity.ProjectileImpactEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ProjectileHitEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private final ProjectileImpactEvent forgeEvent;
	
	public ProjectileHitEvent(ServerPlayerPatch playerpatch, ProjectileImpactEvent forgeEvent) {
		super(playerpatch, true);
		this.forgeEvent = forgeEvent;
	}
	
	public ProjectileImpactEvent getForgeEvent() {
		return this.forgeEvent;
	}
}