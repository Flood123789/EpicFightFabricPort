package yesman.epicfight.data.conditions.entity;

import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CustomCondition<T extends LivingEntityPatch<?>> implements Condition<T> {
	private final Function<T, Boolean> predicate;
	
	public CustomCondition(Function<T, Boolean> predicate) {
		this.predicate = predicate;
	}
	
	@Override
	public CustomCondition<T> read(CompoundTag tag) {
		// This condition doesn't support json conversion
		return null;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		return null;
	}
	
	@Override
	public boolean predicate(T target) {
		return predicate.apply(target);
	}
	
}
