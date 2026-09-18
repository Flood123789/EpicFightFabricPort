package yesman.epicfight.api.forgeevent;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.event.IModBusEvent;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class WeaponCapabilityPresetRegistryEvent extends Event implements IModBusEvent {
	private final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry;
	
	public WeaponCapabilityPresetRegistryEvent(Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> getTypeEntry() {
		return this.typeEntry;
	}
}