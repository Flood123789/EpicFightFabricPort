package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ShieldBlockEvent extends LivingEvent {
	private final DamageSource source;
	private final float originalBlockedDamage;
	private float blockedDamage;
	private boolean shieldTakesDamage = true;

	public ShieldBlockEvent(LivingEntity entity, DamageSource source, float blockedDamage) {
		super(entity);
		this.source = source;
		this.originalBlockedDamage = blockedDamage;
		this.blockedDamage = blockedDamage;
	}

	public DamageSource getDamageSource() {
		return this.source;
	}

	public float getOriginalBlockedDamage() {
		return this.originalBlockedDamage;
	}

	public float getBlockedDamage() {
		return this.blockedDamage;
	}

	public void setBlockedDamage(float blockedDamage) {
		this.blockedDamage = blockedDamage;
	}

	public boolean shieldTakesDamage() {
		return this.shieldTakesDamage;
	}

	public void setShieldTakesDamage(boolean takesDamage) {
		this.shieldTakesDamage = takesDamage;
	}
}
