package yesman.epicfight.forgecompat.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class EntityEvent extends Event {
	private final Entity entity;

	public EntityEvent(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return this.entity;
	}

	public static class Size extends EntityEvent {
		private EntityDimensions newSize;
		private final EntityDimensions oldSize;

		public Size(Entity entity, EntityDimensions oldSize, EntityDimensions newSize) {
			super(entity);
			this.oldSize = oldSize;
			this.newSize = newSize;
		}

		public EntityDimensions getOldSize() {
			return this.oldSize;
		}

		public EntityDimensions getNewSize() {
			return this.newSize;
		}

		public void setNewSize(EntityDimensions size) {
			this.newSize = size;
		}
	}
}
