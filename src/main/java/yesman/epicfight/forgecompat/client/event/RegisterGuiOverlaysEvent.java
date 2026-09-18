package yesman.epicfight.forgecompat.client.event;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RegisterGuiOverlaysEvent extends Event {
	public void registerAbove(ResourceLocation other, String name, yesman.epicfight.forgecompat.client.gui.overlay.IGuiOverlay overlay) {
	}

	public void registerBelow(ResourceLocation other, String name, yesman.epicfight.forgecompat.client.gui.overlay.IGuiOverlay overlay) {
	}

	public void registerAboveAll(String name, yesman.epicfight.forgecompat.client.gui.overlay.IGuiOverlay overlay) {
	}

	public void registerBelowAll(String name, yesman.epicfight.forgecompat.client.gui.overlay.IGuiOverlay overlay) {
	}
}
