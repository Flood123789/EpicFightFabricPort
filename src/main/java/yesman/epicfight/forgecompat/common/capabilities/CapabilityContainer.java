package yesman.epicfight.forgecompat.common.capabilities;

import java.util.Collections;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

/** Per-owner storage for lazily attached capabilities. */
public final class CapabilityContainer {
	private Map<ResourceLocation, ICapabilityProvider> providers;
	private boolean attaching;

	Map<ResourceLocation, ICapabilityProvider> getOrAttach(Object owner) {
		synchronized (this) {
			if (this.providers != null) {
				return this.providers;
			}

			// Attachment handlers may query another capability on the same owner.
			if (this.attaching) {
				return Collections.emptyMap();
			}

			this.attaching = true;
		}

		try {
			Map<ResourceLocation, ICapabilityProvider> attached = CapabilityManager.attachProviders(owner);
			synchronized (this) {
				this.providers = attached;
				return this.providers;
			}
		} finally {
			synchronized (this) {
				this.attaching = false;
			}
		}
	}

	void invalidate() {
		Map<ResourceLocation, ICapabilityProvider> invalidated;
		synchronized (this) {
			invalidated = this.providers;
			this.providers = null;
		}

		if (invalidated != null) {
			invalidated.values().forEach(provider -> provider.invalidateCaps());
		}
	}
}
