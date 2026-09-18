package yesman.epicfight.forgecompat.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

@Cancelable
public class EntityTeleportEvent extends EntityEvent {
	private double targetX;
	private double targetY;
	private double targetZ;

	public EntityTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
		super(entity);
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
	}

	public double getTargetX() {
		return this.targetX;
	}

	public void setTargetX(double targetX) {
		this.targetX = targetX;
	}

	public double getTargetY() {
		return this.targetY;
	}

	public void setTargetY(double targetY) {
		this.targetY = targetY;
	}

	public double getTargetZ() {
		return this.targetZ;
	}

	public void setTargetZ(double targetZ) {
		this.targetZ = targetZ;
	}

	public static class EnderEntity extends EntityTeleportEvent {
		private final LivingEntity livingEntity;

		public EnderEntity(LivingEntity entity, double targetX, double targetY, double targetZ) {
			super(entity, targetX, targetY, targetZ);
			this.livingEntity = entity;
		}

		public LivingEntity getEntityLiving() {
			return this.livingEntity;
		}
	}
}
