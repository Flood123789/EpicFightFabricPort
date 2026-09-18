package yesman.epicfight.forgecompat.common;

import yesman.epicfight.forgecompat.eventbus.EventBusImpl;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;

public class MinecraftForge {
	public static final IEventBus EVENT_BUS = new EventBusImpl();
}
