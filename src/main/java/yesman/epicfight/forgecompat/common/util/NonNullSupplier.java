package yesman.epicfight.forgecompat.common.util;

@FunctionalInterface
public interface NonNullSupplier<T> {
	T get();
}
