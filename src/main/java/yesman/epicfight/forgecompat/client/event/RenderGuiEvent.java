package yesman.epicfight.forgecompat.client.event;

import net.minecraft.client.gui.GuiGraphics;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class RenderGuiEvent extends Event {
	private final GuiGraphics guiGraphics;
	private final float partialTick;

	public RenderGuiEvent(GuiGraphics guiGraphics, float partialTick) {
		this.guiGraphics = guiGraphics;
		this.partialTick = partialTick;
	}

	public GuiGraphics getGuiGraphics() {
		return this.guiGraphics;
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	@Cancelable
	public static class Pre extends RenderGuiEvent {
		public Pre(GuiGraphics guiGraphics, float partialTick) {
			super(guiGraphics, partialTick);
		}
	}

	public static class Post extends RenderGuiEvent {
		public Post(GuiGraphics guiGraphics, float partialTick) {
			super(guiGraphics, partialTick);
		}
	}
}
