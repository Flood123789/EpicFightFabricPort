package yesman.epicfight.data.conditions.entity;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class HealthPoint extends EntityPatchCondition {
	private float health;
	private Comparator comparator;
	
	public HealthPoint() {
		this.health = 0.0F;
	}
	
	public HealthPoint(float health, Comparator comparator) {
		this.health = health;
		this.comparator = comparator;
	}
	
	@Override
	public HealthPoint read(CompoundTag tag) {
		this.health = this.assertTag("health", "decimal", tag, NumericTag.class, CompoundTag::getFloat);
		this.comparator = this.assertEnumTag("comparator", Comparator.class, tag);
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("comparator", this.comparator.toString().toLowerCase(Locale.ROOT));
		tag.putFloat("health", this.health);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		switch (this.comparator) {
		case LESS_ABSOLUTE:
			return this.health > target.getOriginal().getHealth();
		case GREATER_ABSOLUTE:
			return this.health < target.getOriginal().getHealth();
		case LESS_RATIO:
			return this.health > target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
		case GREATER_RATIO:
			return this.health < target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
		}
		
		return true;
	}
	
	public enum Comparator {
		GREATER_ABSOLUTE, LESS_ABSOLUTE, GREATER_RATIO, LESS_RATIO
	}
}
