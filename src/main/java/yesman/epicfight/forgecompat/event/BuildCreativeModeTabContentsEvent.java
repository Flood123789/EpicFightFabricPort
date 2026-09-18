package yesman.epicfight.forgecompat.event;

import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.event.lifecycle.IModBusEvent;

public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent {
	private final CreativeModeTab tab;
	private final Consumer<ItemStack> stackConsumer;

	public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, Consumer<ItemStack> stackConsumer) {
		this.tab = tab;
		this.stackConsumer = stackConsumer;
	}

	public CreativeModeTab getTab() {
		return this.tab;
	}

	public void accept(ItemStack stack) {
		if (this.stackConsumer != null) {
			this.stackConsumer.accept(stack);
		}
	}
}
