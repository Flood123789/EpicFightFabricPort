package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class MobEffectEvent extends LivingEvent {
	private final MobEffectInstance effectInstance;

	public MobEffectEvent(LivingEntity entity, MobEffectInstance effectInstance) {
		super(entity);
		this.effectInstance = effectInstance;
	}

	public MobEffectInstance getEffectInstance() {
		return this.effectInstance;
	}

	public static class Added extends MobEffectEvent {
		private final MobEffectInstance oldEffectInstance;

		public Added(LivingEntity entity, MobEffectInstance effectInstance, MobEffectInstance oldEffectInstance) {
			super(entity, effectInstance);
			this.oldEffectInstance = oldEffectInstance;
		}

		public MobEffectInstance getOldEffectInstance() {
			return this.oldEffectInstance;
		}
	}

	public static class Remove extends MobEffectEvent {
		public Remove(LivingEntity entity, MobEffectInstance effectInstance) {
			super(entity, effectInstance);
		}
	}

	public static class Expired extends MobEffectEvent {
		public Expired(LivingEntity entity, MobEffectInstance effectInstance) {
			super(entity, effectInstance);
		}
	}
}
