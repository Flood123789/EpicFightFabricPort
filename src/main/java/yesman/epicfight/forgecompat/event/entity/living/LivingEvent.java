package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.event.entity.EntityEvent;

public class LivingEvent extends EntityEvent {
	public LivingEvent(LivingEntity entity) {
		super(entity);
	}

	@Override
	public LivingEntity getEntity() {
		return (LivingEntity) super.getEntity();
	}

	public static class LivingTickEvent extends LivingEvent {
		public LivingTickEvent(LivingEntity entity) {
			super(entity);
		}
	}

	public static class LivingJumpEvent extends LivingEvent {
		public LivingJumpEvent(LivingEntity entity) {
			super(entity);
		}
	}
}
