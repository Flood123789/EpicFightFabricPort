package yesman.epicfight.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.common.capabilities.Capability;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.forgecompat.common.capabilities.ICapabilityProvider;
import yesman.epicfight.forgecompat.common.util.LazyOptional;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack implements ICapabilityProvider {
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		return CapabilityManager.getCapability(this, cap, side);
	}
}
