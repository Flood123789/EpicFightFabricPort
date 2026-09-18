package yesman.epicfight.forgecompat.fml;

import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ModLoader {
	private static final ModLoader INSTANCE = new ModLoader();

	private ModLoader() {
	}

	public static ModLoader get() {
		return INSTANCE;
	}

	public <T extends Event> T postEvent(T event) {
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
}
