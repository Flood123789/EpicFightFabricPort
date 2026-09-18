package yesman.epicfight.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import yesman.epicfight.forgecompat.common.capabilities.Capability;
import yesman.epicfight.forgecompat.common.capabilities.CapabilityManager;
import yesman.epicfight.forgecompat.common.capabilities.ICapabilityProvider;
import yesman.epicfight.forgecompat.common.util.LazyOptional;

@Mixin(value = LevelChunk.class)
public abstract class MixinLevelChunk implements ICapabilityProvider {
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		return CapabilityManager.getCapability(this, cap, side);
	}
}
