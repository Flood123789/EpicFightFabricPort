package yesman.epicfight.forgecompat.eventbus.api;

import java.util.function.Consumer;

public interface IEventBus {
	void register(Object object);

	<T extends Event> void addListener(Consumer<T> consumer);

	<T extends Event> void addListener(EventPriority priority, Consumer<T> consumer);

	<T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventClass, Consumer<T> consumer);

	<T extends Event, F> void addGenericListener(Class<F> genericClass, Consumer<T> consumer);

	boolean post(Event event);
}
