package yesman.epicfight.network.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ClientboundPacketBridge {
	private static final String HANDLER_CLASS = "yesman.epicfight.network.server.ClientboundPacketHandlers";
	private static final Map<Class<?>, Method> HANDLERS = new ConcurrentHashMap<>();

	private ClientboundPacketBridge() {
	}

	static void handle(Object packet) {
		if (packet == null) {
			return;
		}

		try {
			Method handler = HANDLERS.computeIfAbsent(packet.getClass(), ClientboundPacketBridge::findHandler);
			handler.invoke(null, packet);
		} catch (IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalStateException("Failed to handle Epic Fight clientbound packet " + packet.getClass().getName(), exception);
		}
	}

	private static Method findHandler(Class<?> packetClass) {
		try {
			return Class.forName(HANDLER_CLASS).getMethod("handle", packetClass);
		} catch (ClassNotFoundException | NoSuchMethodException exception) {
			throw new IllegalStateException("Missing Epic Fight clientbound packet handler for " + packetClass.getName(), exception);
		}
	}
}
