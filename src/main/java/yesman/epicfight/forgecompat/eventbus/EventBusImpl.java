package yesman.epicfight.forgecompat.eventbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.eventbus.api.EventPriority;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.forgecompat.eventbus.api.SubscribeEvent;
import yesman.epicfight.forgecompat.event.AttachCapabilitiesEvent;

public class EventBusImpl implements IEventBus {
	private final Map<Class<?>, List<ListenerEntry>> listeners = new ConcurrentHashMap<>();

	private record ListenerEntry(EventPriority priority, boolean receiveCanceled, Class<?> genericClass, Class<?> targetEventClass, Consumer<Event> handler) implements Comparable<ListenerEntry> {
		@Override
		public int compareTo(ListenerEntry o) {
			return this.priority.compareTo(o.priority);
		}
	}

	@Override
	public void register(Object object) {
		if (object == null) return;
		boolean isClass = object instanceof Class<?>;
		Class<?> clazz = isClass ? (Class<?>) object : object.getClass();

		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(SubscribeEvent.class)) {
				if (isClass && !Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				if (!isClass && Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				Class<?>[] paramTypes = method.getParameterTypes();
				if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0])) {
					continue;
				}
				@SuppressWarnings("unchecked")
				Class<? extends Event> eventType = (Class<? extends Event>) paramTypes[0];
				SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
				method.setAccessible(true);
				Object target = isClass ? null : object;

				Class<?> genericClass = resolveGenericEventType(method);
				addListener(annotation.priority(), annotation.receiveCanceled(), genericClass, eventType, event -> {
					try {
						method.invoke(target, event);
					} catch (Exception e) {
						throw new RuntimeException("Error invoking event listener " + method + " on " + target, e);
					}
				});
			}
		}
	}

	@Override
	public <T extends Event> void addListener(Consumer<T> consumer) {
		addListener(EventPriority.NORMAL, false, null, consumer);
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
		addListener(priority, false, null, consumer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventClass, Consumer<T> consumer) {
		Class<?> targetClass = eventClass;
		if (targetClass == null) {
			targetClass = resolveEventType(consumer);
			if (targetClass == null) {
				targetClass = Event.class;
			}
		}
		addListener(priority, receiveCanceled, null, targetClass, (Consumer<Event>) consumer);
	}

	private void addListener(EventPriority priority, boolean receiveCanceled, Class<?> genericClass, Class<?> targetClass, Consumer<Event> consumer) {
		List<ListenerEntry> list = listeners.computeIfAbsent(targetClass, k -> Collections.synchronizedList(new ArrayList<>()));
		list.add(new ListenerEntry(priority, receiveCanceled, genericClass, targetClass, consumer));
		Collections.sort(list);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Event, F> void addGenericListener(Class<F> genericClass, Consumer<T> consumer) {
		List<ListenerEntry> list = listeners.computeIfAbsent(genericClass, k -> Collections.synchronizedList(new ArrayList<>()));
		list.add(new ListenerEntry(EventPriority.NORMAL, false, genericClass, null, (Consumer<Event>) consumer));
		Collections.sort(list);
	}

	@Override
	public boolean post(Event event) {
		if (event == null) return false;
		Class<?> eventClass = event.getClass();
		List<ListenerEntry> toRun = new ArrayList<>();

		for (Map.Entry<Class<?>, List<ListenerEntry>> entry : listeners.entrySet()) {
			if (entry.getKey().isAssignableFrom(eventClass)) {
				synchronized (entry.getValue()) {
					toRun.addAll(entry.getValue());
				}
			}
		}

		Collections.sort(toRun);

		for (ListenerEntry entry : toRun) {
			if (event.isCanceled() && !entry.receiveCanceled()) {
				continue;
			}
			if (entry.targetEventClass() != null && entry.targetEventClass() != Event.class && !entry.targetEventClass().isInstance(event)) {
				continue;
			}
			if (entry.genericClass() != null && event instanceof AttachCapabilitiesEvent<?> attachEvent
					&& !entry.genericClass().isInstance(attachEvent.getObject())) {
				continue;
			}
			entry.handler().accept(event);
		}

		return event.isCanceled();
	}

	private Class<?> resolveGenericEventType(Method method) {
		Type parameter = method.getGenericParameterTypes()[0];
		if (parameter instanceof ParameterizedType parameterizedType
				&& parameterizedType.getRawType() == AttachCapabilitiesEvent.class) {
			Type argument = parameterizedType.getActualTypeArguments()[0];
			if (argument instanceof Class<?> argumentClass) {
				return argumentClass;
			}
		}
		return null;
	}

	private Class<?> resolveEventType(Consumer<?> consumer) {
		if (consumer == null) return null;
		for (Method method : consumer.getClass().getMethods()) {
			if ("accept".equals(method.getName()) && !method.isBridge() && !method.isSynthetic()) {
				Class<?>[] params = method.getParameterTypes();
				if (params.length == 1 && Event.class.isAssignableFrom(params[0]) && params[0] != Event.class) {
					return params[0];
				}
			}
		}
		try {
			Method writeReplace = consumer.getClass().getDeclaredMethod("writeReplace");
			writeReplace.setAccessible(true);
			Object replacement = writeReplace.invoke(consumer);
			if (replacement instanceof java.lang.invoke.SerializedLambda lambda) {
				String desc = lambda.getImplMethodSignature();
				Class<?> clazz = parseClassFromDescriptor(desc, consumer.getClass().getClassLoader());
				if (clazz != null && Event.class.isAssignableFrom(clazz)) {
					return clazz;
				}
			}
		} catch (Throwable ignored) {
		}
		try {
			Class<?> clazz = consumer.getClass();
			Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
			getConstantPool.setAccessible(true);
			Object cp = getConstantPool.invoke(clazz);
			Method getSize = cp.getClass().getDeclaredMethod("getSize");
			getSize.setAccessible(true);
			Method getMemberRefInfoAt = cp.getClass().getDeclaredMethod("getMemberRefInfoAt", int.class);
			getMemberRefInfoAt.setAccessible(true);
			int size = (int) getSize.invoke(cp);
			for (int i = 1; i < size; i++) {
				try {
					String[] info = (String[]) getMemberRefInfoAt.invoke(cp, i);
					if (info != null && info.length >= 3) {
						String desc = info[2];
						Class<?> paramClass = parseClassFromDescriptor(desc, consumer.getClass().getClassLoader());
						if (paramClass != null && Event.class.isAssignableFrom(paramClass) && paramClass != Event.class) {
							return paramClass;
						}
					}
				} catch (Throwable ignored) {
				}
			}
		} catch (Throwable ignored) {
		}
		return null;
	}

	private Class<?> parseClassFromDescriptor(String desc, ClassLoader classLoader) {
		if (desc != null && desc.startsWith("(") && desc.contains(")")) {
			String paramDesc = desc.substring(1, desc.indexOf(')'));
			if (paramDesc.startsWith("L") && paramDesc.endsWith(";")) {
				String className = paramDesc.substring(1, paramDesc.length() - 1).replace('/', '.');
				try {
					return Class.forName(className, false, classLoader);
				} catch (Throwable ignored) {
				}
			}
		}
		return null;
	}
}
