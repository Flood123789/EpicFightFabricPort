package yesman.epicfight.forgecompat.registries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class RegistryManager {
	public static final RegistryManager ACTIVE = new RegistryManager();
	private final Map<ResourceLocation, ForgeRegistry<?>> registries = new ConcurrentHashMap<>();

	public RegistryManager() {
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T> ForgeRegistry<T> getRegistry(ResourceLocation name) {
		ForgeRegistry<?> reg = registries.get(name);
		if (reg == null) {
			ForgeRegistry<?> builtIn = ForgeRegistries.getRegistryByName(name);
			if (builtIn != null) {
				registries.put(name, builtIn);
				return (ForgeRegistry<T>) builtIn;
			}
		}
		return (ForgeRegistry<T>) reg;
	}

	public <T> ForgeRegistry<T> getRegistry(net.minecraft.resources.ResourceKey<? extends Registry<T>> key) {
		return getRegistry(key.location());
	}

	@SuppressWarnings("unchecked")
	public <T> ForgeRegistry<T> createRegistry(ResourceLocation name, RegistryBuilder<T> builder) {
		ForgeRegistry<T> existing = (ForgeRegistry<T>) getRegistry(name);
		if (existing != null) {
			return existing;
		}
		ForgeRegistry<T> registry = new ForgeRegistry<>(name, null);
		if (builder != null && builder.getCallbacks() != null) {
			for (Object cb : builder.getCallbacks()) {
				registry.addCallback(cb);
			}
		}
		registries.put(name, registry);
		registry.fireCreate();
		return registry;
	}

	public <T> void registerRegistry(ResourceLocation name, ForgeRegistry<T> registry) {
		registries.put(name, registry);
	}
}
