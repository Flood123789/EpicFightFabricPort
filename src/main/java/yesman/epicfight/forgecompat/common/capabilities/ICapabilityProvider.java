package yesman.epicfight.forgecompat.common.capabilities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import yesman.epicfight.forgecompat.common.util.LazyOptional;

public interface ICapabilityProvider {
	<T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side);

	default <T> LazyOptional<T> getCapability(Capability<T> cap) {
		return getCapability(cap, (Direction) null);
	}

	@SuppressWarnings("unchecked")
	static <T> LazyOptional<T> getCapability(Object provider, Capability<T> cap, @Nullable Direction side) {
		if (provider instanceof ICapabilityProvider capProvider) {
			return capProvider.getCapability(cap, side);
		}
		return CapabilityManager.getCapability(provider, cap, side);
	}

	static <T> LazyOptional<T> getCapability(Object provider, Capability<T> cap) {
		return getCapability(provider, cap, null);
	}

	default void reviveCaps() {}
	default void invalidateCaps() {}

	static void reviveCaps(Object provider) {
		CapabilityManager.reviveCapabilities(provider);
	}

	static void invalidateCaps(Object provider) {
		CapabilityManager.invalidateCapabilities(provider);
	}
}
