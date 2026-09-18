package yesman.epicfight.forgecompat.common.capabilities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.MapMaker;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.common.util.LazyOptional;
import yesman.epicfight.forgecompat.event.AttachCapabilitiesEvent;

public class CapabilityManager {
	public static final CapabilityManager INSTANCE = new CapabilityManager();
	private final Map<String, Capability<?>> capabilities = new ConcurrentHashMap<>();
	// Capabilities belong to an exact game object. In an integrated game, the client and
	// server player represent the same account and can compare equal, but must never share
	// providers: one needs LocalPlayerPatch and the other needs ServerPlayerPatch.
	// The attachment key is kept because it names the provider in serialized NBT.
	private final Map<Object, Map<ResourceLocation, ICapabilityProvider>> attachedCapabilities = new MapMaker().weakKeys().makeMap();

	private CapabilityManager() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Capability<T> get(CapabilityToken<T> token) {
		String typeName = token.getType();
		return (Capability<T>) get(typeName, false);
	}

	@SuppressWarnings("unchecked")
	public static Capability<?> get(String name, boolean isInterface) {
		return INSTANCE.capabilities.computeIfAbsent(name, k -> new Capability<>(name, null));
	}

	public <T> void register(Class<T> clazz) {
		String name = clazz.getName();
		capabilities.put(name, new Capability<>(name, clazz));
	}

	public static <T> LazyOptional<T> getCapability(Object owner, Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
		if (owner == null || cap == null) {
			return LazyOptional.empty();
		}

		for (ICapabilityProvider provider : INSTANCE.getOrAttach(owner).values()) {
			LazyOptional<T> result = provider.getCapability(cap, side);
			if (result.isPresent()) {
				return result;
			}
		}

		return LazyOptional.empty();
	}

	/**
	 * Writes every attached {@link ICapabilitySerializable} provider of @param owner into one compound,
	 * keyed by the ResourceLocation the provider was attached with. Mirrors Forge's {@code serializeCaps}.
	 */
	public static CompoundTag serializeCapabilities(Object owner) {
		CompoundTag serialized = new CompoundTag();

		if (owner == null) {
			return serialized;
		}

		for (Map.Entry<ResourceLocation, ICapabilityProvider> entry : INSTANCE.getOrAttach(owner).entrySet()) {
			if (entry.getValue() instanceof ICapabilitySerializable<?> serializable) {
				Tag providerData = serializable.serializeNBT();

				if (providerData != null) {
					serialized.put(entry.getKey().toString(), providerData);
				}
			}
		}

		return serialized;
	}

	/**
	 * Restores the providers of @param owner from a compound written by {@link #serializeCapabilities}.
	 * Keys with no matching attached provider are ignored, as are providers absent from the compound.
	 */
	@SuppressWarnings("unchecked")
	public static void deserializeCapabilities(Object owner, CompoundTag serialized) {
		if (owner == null || serialized == null) {
			return;
		}

		for (Map.Entry<ResourceLocation, ICapabilityProvider> entry : INSTANCE.getOrAttach(owner).entrySet()) {
			if (!(entry.getValue() instanceof ICapabilitySerializable<?> serializable)) {
				continue;
			}

			String key = entry.getKey().toString();

			if (serialized.contains(key, Tag.TAG_COMPOUND)) {
				((ICapabilitySerializable<CompoundTag>)serializable).deserializeNBT(serialized.getCompound(key));
			}
		}
	}

	private Map<ResourceLocation, ICapabilityProvider> getOrAttach(Object owner) {
		if (owner instanceof ICapabilityOwner capabilityOwner) {
			return capabilityOwner.epicfight$getCapabilityContainer().getOrAttach(owner);
		}

		synchronized (this.attachedCapabilities) {
			Map<ResourceLocation, ICapabilityProvider> existing = this.attachedCapabilities.get(owner);
			if (existing != null) {
				return existing;
			}

			// Install a placeholder before posting the event. Some attachment handlers query
			// another capability on the same owner, so this also prevents recursive attachment.
			this.attachedCapabilities.put(owner, Collections.emptyMap());
		}

		try {
			Map<ResourceLocation, ICapabilityProvider> providers = attachProviders(owner);

			synchronized (this.attachedCapabilities) {
				this.attachedCapabilities.put(owner, providers);
			}

			return providers;
		} catch (RuntimeException | Error exception) {
			synchronized (this.attachedCapabilities) {
				this.attachedCapabilities.remove(owner);
			}
			throw exception;
		}
	}

	static Map<ResourceLocation, ICapabilityProvider> attachProviders(Object owner) {
		AttachCapabilitiesEvent<Object> event = new AttachCapabilitiesEvent<>(owner);
		MinecraftForge.EVENT_BUS.post(event);
		return Collections.unmodifiableMap(new LinkedHashMap<>(event.getCapabilities()));
	}

	public static void invalidateCapabilities(Object owner) {
		if (owner instanceof ICapabilityOwner capabilityOwner) {
			capabilityOwner.epicfight$getCapabilityContainer().invalidate();
			return;
		}

		Map<ResourceLocation, ICapabilityProvider> providers;
		synchronized (INSTANCE.attachedCapabilities) {
			providers = INSTANCE.attachedCapabilities.remove(owner);
		}

		if (providers != null) {
			providers.values().forEach(provider -> provider.invalidateCaps());
		}
	}

	public static void reviveCapabilities(Object owner) {
		invalidateCapabilities(owner);
	}
}
