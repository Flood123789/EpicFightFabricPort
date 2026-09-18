package yesman.epicfight.forgecompat.event.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class SpawnPlacementRegisterEvent extends Event {
	public enum Operation {
		AND,
		OR,
		REPLACE
	}

	public <T extends net.minecraft.world.entity.Mob> void register(EntityType<T> entityType, SpawnPlacements.Type placementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
		SpawnPlacements.register(entityType, placementType, heightmapType, predicate);
	}
}
