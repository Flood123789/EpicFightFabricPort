package yesman.epicfight.forgecompat.common.util;

@FunctionalInterface
public interface NonNullConsumer<T> {
	void accept(T t);
}
