package yesman.epicfight.forgecompat.registries;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface IForgeRegistry<V> extends Iterable<V> {
	ResourceLocation getRegistryName();

	@Nullable
	V getValue(ResourceLocation key);

	@Nullable
	ResourceKey<V> getResourceKey(V value);

	@Nullable
	ResourceLocation getKey(V value);

	boolean containsKey(ResourceLocation key);

	boolean containsValue(V value);

	Set<ResourceLocation> getKeys();

	Collection<V> getValues();

	Set<Map.Entry<ResourceKey<V>, V>> getEntries();

	com.mojang.serialization.Codec<V> getCodec();

	void register(ResourceLocation key, V value);

	default void register(String key, V value) {
		register(new ResourceLocation(key), value);
	}

	<T> T getSlaveMap(ResourceLocation name, Class<T> type);

	void setSlaveMap(ResourceLocation name, Object obj);

	@Override
	default java.util.Iterator<V> iterator() {
		return getValues().iterator();
	}

	interface BakeCallback<V> {
		void onBake(IForgeRegistryInternal<V> owner, RegistryManager stage);
	}

	interface CreateCallback<V> {
		void onCreate(IForgeRegistryInternal<V> owner, RegistryManager stage);
	}

	interface ClearCallback<V> {
		void onClear(IForgeRegistryInternal<V> owner, RegistryManager stage);
	}
}
