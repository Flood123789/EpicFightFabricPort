package yesman.epicfight.data.conditions.entity;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetInEyeHeight extends EntityPatchCondition {
	@Override
	public TargetInEyeHeight read(CompoundTag tag) {
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		return new CompoundTag();
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		double veticalDistance = Math.abs(target.getOriginal().getY() - target.getTarget().getY());
		return veticalDistance < target.getOriginal().getEyeHeight();
	}
	
}
