package yesman.epicfight.forgecompat.fml.event.lifecycle;

import yesman.epicfight.forgecompat.eventbus.api.Event;

public class FMLConstructModEvent extends Event implements IModBusEvent {
	public void enqueueWork(Runnable runnable) {
		runnable.run();
	}
}
