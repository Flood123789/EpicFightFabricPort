package yesman.epicfight.forgecompat.event.entity.player;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ItemTooltipEvent extends Event {
	private final ItemStack itemStack;
	private final Player entity;
	private final List<Component> toolTip;
	private final TooltipFlag flags;

	public ItemTooltipEvent(ItemStack itemStack, Player entity, List<Component> toolTip, TooltipFlag flags) {
		this.itemStack = itemStack;
		this.entity = entity;
		this.toolTip = toolTip;
		this.flags = flags;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public Player getEntity() {
		return this.entity;
	}

	public List<Component> getToolTip() {
		return this.toolTip;
	}

	public TooltipFlag getFlags() {
		return this.flags;
	}
}
