package yesman.epicfight.forgecompat.event;

import java.util.Collection;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ItemAttributeModifierEvent extends Event {
	private final ItemStack itemStack;
	private final EquipmentSlot slotType;
	private final BiConsumer<Attribute, AttributeModifier> addModifier;

	public ItemAttributeModifierEvent(ItemStack itemStack, EquipmentSlot slotType, BiConsumer<Attribute, AttributeModifier> addModifier) {
		this.itemStack = itemStack;
		this.slotType = slotType;
		this.addModifier = addModifier;
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public EquipmentSlot getSlotType() {
		return this.slotType;
	}

	public void addModifier(Attribute attribute, AttributeModifier modifier) {
		if (this.addModifier != null) {
			this.addModifier.accept(attribute, modifier);
		}
	}
}
