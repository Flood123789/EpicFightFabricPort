package yesman.epicfight.forgecompat.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ForgeRegistry<V> implements IForgeRegistryInternal<V> {
	private final ResourceLocation name;
	private final ResourceKey<Registry<V>> registryKey;
	private final Registry<V> vanillaRegistry;
	private final Map<ResourceLocation, V> customEntries = new ConcurrentHashMap<>();
	private final Map<V, ResourceLocation> reverseMap = new ConcurrentHashMap<>();
	private final List<Object> callbacks = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public ForgeRegistry(ResourceLocation name, @Nullable Registry<V> vanillaRegistry) {
		this.name = name;
		this.registryKey = ResourceKey.createRegistryKey(name);
		this.vanillaRegistry = vanillaRegistry;
	}

	public void addCallback(Object callback) {
		if (callback != null && !this.callbacks.contains(callback)) {
			this.callbacks.add(callback);
		}
	}

	public void fireCreate() {
		for (Object cb : this.callbacks) {
			if (cb instanceof CreateCallback<?> createCb) {
				@SuppressWarnings("unchecked")
				CreateCallback<V> typed = (CreateCallback<V>) createCb;
				typed.onCreate(this, RegistryManager.ACTIVE);
			}
		}
	}

	public void fireBake() {
		for (Object cb : this.callbacks) {
			if (cb instanceof BakeCallback<?> bakeCb) {
				@SuppressWarnings("unchecked")
				BakeCallback<V> typed = (BakeCallback<V>) bakeCb;
				typed.onBake(this, RegistryManager.ACTIVE);
			}
		}
	}

	public void fireClear() {
		for (Object cb : this.callbacks) {
			if (cb instanceof ClearCallback<?> clearCb) {
				@SuppressWarnings("unchecked")
				ClearCallback<V> typed = (ClearCallback<V>) clearCb;
				typed.onClear(this, RegistryManager.ACTIVE);
			}
		}
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.name;
	}

	@Override
	@Nullable
	public V getValue(ResourceLocation key) {
		if (key == null) return null;
		if (this.vanillaRegistry != null && this.vanillaRegistry.containsKey(key)) {
			return this.vanillaRegistry.get(key);
		}
		return this.customEntries.get(key);
	}

	@Override
	@Nullable
	public ResourceKey<V> getResourceKey(V value) {
		ResourceLocation key = getKey(value);
		return key == null ? null : ResourceKey.create(this.registryKey, key);
	}

	@Override
	@Nullable
	public ResourceLocation getKey(V value) {
		if (value == null) return null;
		if (this.vanillaRegistry != null) {
			ResourceLocation id = this.vanillaRegistry.getKey(value);
			if (id != null) return id;
		}
		return this.reverseMap.get(value);
	}

	@Override
	public boolean containsKey(ResourceLocation key) {
		if (key == null) return false;
		if (this.vanillaRegistry != null && this.vanillaRegistry.containsKey(key)) {
			return true;
		}
		return this.customEntries.containsKey(key);
	}

	@Override
	public boolean containsValue(V value) {
		if (value == null) return false;
		if (this.vanillaRegistry != null) {
			if (this.vanillaRegistry.getKey(value) != null) return true;
		}
		return this.reverseMap.containsKey(value);
	}

	@Override
	public Set<ResourceLocation> getKeys() {
		Set<ResourceLocation> keys = new HashSet<>();
		if (this.vanillaRegistry != null) {
			keys.addAll(this.vanillaRegistry.keySet());
		}
		keys.addAll(this.customEntries.keySet());
		return keys;
	}

	@Override
	public Collection<V> getValues() {
		Set<V> values = new HashSet<>();
		if (this.vanillaRegistry != null) {
			for (V val : this.vanillaRegistry) {
				values.add(val);
			}
		}
		values.addAll(this.customEntries.values());
		return values;
	}

	@Override
	public Set<Map.Entry<ResourceKey<V>, V>> getEntries() {
		Set<Map.Entry<ResourceKey<V>, V>> result = new HashSet<>();
		if (this.vanillaRegistry != null) {
			for (Map.Entry<ResourceKey<V>, V> entry : this.vanillaRegistry.entrySet()) {
				result.add(entry);
			}
		}
		for (Map.Entry<ResourceLocation, V> entry : this.customEntries.entrySet()) {
			ResourceKey<V> key = ResourceKey.create(this.registryKey, entry.getKey());
			result.add(Map.entry(key, entry.getValue()));
		}
		return result;
	}

	private final Map<ResourceLocation, Object> slaveMaps = new ConcurrentHashMap<>();

	@Override
	public com.mojang.serialization.Codec<V> getCodec() {
		if (this.vanillaRegistry != null) {
			return this.vanillaRegistry.byNameCodec();
		}
		return ResourceLocation.CODEC.xmap(this::getValue, this::getKey);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void register(ResourceLocation key, V value) {
		if (key == null || value == null) return;
		this.customEntries.put(key, value);
		this.reverseMap.put(value, key);
		if (this.vanillaRegistry != null && !this.vanillaRegistry.containsKey(key)) {
			Registry.register((Registry<Object>) this.vanillaRegistry, key, value);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getSlaveMap(ResourceLocation name, Class<T> type) {
		return (T) this.slaveMaps.get(name);
	}

	@Override
	public void setSlaveMap(ResourceLocation name, Object obj) {
		if (obj != null) {
			this.slaveMaps.put(name, obj);
		} else {
			this.slaveMaps.remove(name);
		}
	}
}
