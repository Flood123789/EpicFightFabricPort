package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

@Cancelable
public class LivingKnockBackEvent extends LivingEvent {
	private float strength;
	private double ratioX;
	private double ratioZ;

	public LivingKnockBackEvent(LivingEntity entity, float strength, double ratioX, double ratioZ) {
		super(entity);
		this.strength = strength;
		this.ratioX = ratioX;
		this.ratioZ = ratioZ;
	}

	public float getStrength() {
		return this.strength;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public double getRatioX() {
		return this.ratioX;
	}

	public double getRatioZ() {
		return this.ratioZ;
	}
}
