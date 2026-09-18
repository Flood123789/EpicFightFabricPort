package yesman.epicfight.forgecompat.registries;

import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.event.lifecycle.IModBusEvent;

public class NewRegistryEvent extends Event implements IModBusEvent {
	public <T> void create(RegistryBuilder<T> builder) {
		// No-op or bridge creation
	}
}
