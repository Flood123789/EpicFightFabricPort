package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

@Cancelable
public class LivingAttackEvent extends LivingEvent {
	private final DamageSource source;
	private final float amount;

	public LivingAttackEvent(LivingEntity entity, DamageSource source, float amount) {
		super(entity);
		this.source = source;
		this.amount = amount;
	}

	public DamageSource getSource() {
		return this.source;
	}

	public float getAmount() {
		return this.amount;
	}
}
