package yesman.epicfight.forgecompat.client.extensions.common;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IClientItemExtensions {
	IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

	static IClientItemExtensions of(ItemStack stack) {
		return DEFAULT;
	}

	default HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
		return original;
	}
}
