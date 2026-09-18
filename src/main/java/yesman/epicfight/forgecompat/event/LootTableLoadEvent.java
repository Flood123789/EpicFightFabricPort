package yesman.epicfight.forgecompat.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class LootTableLoadEvent extends Event {
	private final ResourceLocation name;
	private final LootTable table;

	public LootTableLoadEvent(ResourceLocation name, LootTable table) {
		this.name = name;
		this.table = table;
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public LootTable getTable() {
		return this.table;
	}
}
