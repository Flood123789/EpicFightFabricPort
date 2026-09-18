package yesman.epicfight.forgecompat.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.common.capabilities.ICapabilityProvider;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class AttachCapabilitiesEvent<T> extends Event {
	private final T object;
	private final Map<ResourceLocation, ICapabilityProvider> capabilities = new ConcurrentHashMap<>();

	public AttachCapabilitiesEvent(T object) {
		this.object = object;
	}

	public T getObject() {
		return this.object;
	}

	public void addCapability(ResourceLocation key, ICapabilityProvider provider) {
		this.capabilities.put(key, provider);
	}

	public Map<ResourceLocation, ICapabilityProvider> getCapabilities() {
		return this.capabilities;
	}

	public void addListener(Runnable listener) {
	}
}
