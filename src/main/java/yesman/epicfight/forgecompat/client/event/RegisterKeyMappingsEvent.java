package yesman.epicfight.forgecompat.client.event;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterKeyMappingsEvent extends Event {
	public void register(KeyMapping keyMapping) {
		KeyBindingHelper.registerKeyBinding(keyMapping);
	}
}
