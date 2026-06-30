package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.Condition.EntityCondition;

public class HasCustomTag extends EntityCondition {
	private final Set<String> allowedTags;
	
	public HasCustomTag(ListTag allowedTags) {
		this.allowedTags = allowedTags.stream().map(Tag::getAsString).collect(Collectors.toUnmodifiableSet());
	}
	
	@Override
	public Condition<Entity> read(CompoundTag tag) {
		// This condition doesn't support json conversion
		return null;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		return null;
	}
	
	@Override
	public boolean predicate(Entity target) {
		for (String tag : this.allowedTags) {
			if (target.getTags().contains(tag)) {
				return true;
			}
		}
		
		return false;
	}
	
}
