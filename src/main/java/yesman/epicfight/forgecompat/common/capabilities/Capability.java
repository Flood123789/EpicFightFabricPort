package yesman.epicfight.forgecompat.common.capabilities;

import org.jetbrains.annotations.Nullable;

import yesman.epicfight.forgecompat.common.util.LazyOptional;

public class Capability<T> {
	private final String name;
	private final Class<T> clazz;

	public Capability(String name, Class<T> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return this.name;
	}

	public boolean isRegistered() {
		return true;
	}

	public <R> LazyOptional<R> orEmpty(Capability<R> toCheck, LazyOptional<T> inst) {
		return this == toCheck ? inst.cast() : LazyOptional.empty();
	}

	public Class<T> getClazz() {
		return this.clazz;
	}
}
