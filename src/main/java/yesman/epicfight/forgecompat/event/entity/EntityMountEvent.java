package yesman.epicfight.forgecompat.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityMountEvent extends EntityEvent {
	private final Entity entityMounting;
	private final Entity entityBeingMounted;
	private final Level level;
	private final boolean isMounting;

	public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, Level level, boolean isMounting) {
		super(entityMounting);
		this.entityMounting = entityMounting;
		this.entityBeingMounted = entityBeingMounted;
		this.level = level;
		this.isMounting = isMounting;
	}

	public Entity getEntityMounting() {
		return this.entityMounting;
	}

	public Entity getEntityBeingMounted() {
		return this.entityBeingMounted;
	}

	public Level getLevel() {
		return this.level;
	}

	public boolean isMounting() {
		return this.isMounting;
	}
}
