package yesman.epicfight.data.conditions.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RandomChance extends EntityPatchCondition {
	private float chance;
	
	public RandomChance() {
		this.chance = 0.0F;
	}
	
	public RandomChance(float chance) {
		this.chance = chance;
	}
	
	@Override
	public RandomChance read(CompoundTag tag) {
		this.chance = this.assertTag("chance", "decimal", tag, NumericTag.class, CompoundTag::getFloat);
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("chance", this.chance);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		return target.getOriginal().getRandom().nextFloat() < this.chance;
	}
	
}
