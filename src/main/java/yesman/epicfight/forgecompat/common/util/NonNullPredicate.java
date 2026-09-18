package yesman.epicfight.forgecompat.common.util;

@FunctionalInterface
public interface NonNullPredicate<T> {
	boolean test(T t);
}
