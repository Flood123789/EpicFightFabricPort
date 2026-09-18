package yesman.epicfight.forgecompat.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;

public class LivingEntityUseItemEvent extends LivingEvent {
	private final ItemStack item;
	private int duration;

	public LivingEntityUseItemEvent(LivingEntity entity, ItemStack item, int duration) {
		super(entity);
		this.item = item;
		this.duration = duration;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public int getDuration() {
		return this.duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Cancelable
	public static class Start extends LivingEntityUseItemEvent {
		public Start(LivingEntity entity, ItemStack item, int duration) {
			super(entity, item, duration);
		}
	}

	@Cancelable
	public static class Tick extends LivingEntityUseItemEvent {
		public Tick(LivingEntity entity, ItemStack item, int duration) {
			super(entity, item, duration);
		}
	}

	@Cancelable
	public static class Stop extends LivingEntityUseItemEvent {
		public Stop(LivingEntity entity, ItemStack item, int duration) {
			super(entity, item, duration);
		}
	}

	public static class Finish extends LivingEntityUseItemEvent {
		public Finish(LivingEntity entity, ItemStack item, int duration) {
			super(entity, item, duration);
		}
	}
}
