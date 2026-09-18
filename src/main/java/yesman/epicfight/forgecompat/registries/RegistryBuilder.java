package yesman.epicfight.forgecompat.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public class RegistryBuilder<T> {
	private ResourceLocation name;
	private final List<Object> callbacks = new ArrayList<>();

	public RegistryBuilder() {
	}

	public static <T> RegistryBuilder<T> of(ResourceLocation name) {
		RegistryBuilder<T> builder = new RegistryBuilder<>();
		builder.setName(name);
		return builder;
	}

	public RegistryBuilder<T> setName(ResourceLocation name) {
		this.name = name;
		return this;
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public RegistryBuilder<T> addCallback(Object callback) {
		if (callback != null && !this.callbacks.contains(callback)) {
			this.callbacks.add(callback);
		}
		return this;
	}

	public List<Object> getCallbacks() {
		return this.callbacks;
	}
}
