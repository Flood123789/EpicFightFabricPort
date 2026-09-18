package yesman.epicfight.forgecompat.common.capabilities;

import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterCapabilitiesEvent extends Event {
	public <T> void register(Class<T> type) {
		CapabilityManager.INSTANCE.register(type);
	}
}
