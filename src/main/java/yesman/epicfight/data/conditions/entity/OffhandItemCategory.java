package yesman.epicfight.data.conditions.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class OffhandItemCategory extends EntityPatchCondition {
	private WeaponCategory category;
	
	@Override
	public OffhandItemCategory read(CompoundTag tag) {
		this.category = this.assertExtendableEnumTag("category", WeaponCategory.ENUM_MANAGER, tag);
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("category", this.category.toString());
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		return target.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == this.category;
	}
	
}
