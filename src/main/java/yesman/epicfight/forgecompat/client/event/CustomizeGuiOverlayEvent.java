package yesman.epicfight.forgecompat.client.event;

import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class CustomizeGuiOverlayEvent extends Event {
	@Cancelable
	public static class Chat extends CustomizeGuiOverlayEvent {
	}

	@Cancelable
	public static class BossEventProgress extends CustomizeGuiOverlayEvent {
		private final net.minecraft.client.gui.GuiGraphics guiGraphics;
		private final int x;
		private final int y;
		private final net.minecraft.world.BossEvent bossEvent;

		public BossEventProgress(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, net.minecraft.world.BossEvent bossEvent) {
			this.guiGraphics = guiGraphics;
			this.x = x;
			this.y = y;
			this.bossEvent = bossEvent;
		}

		public BossEventProgress() {
			this.guiGraphics = null;
			this.x = 0;
			this.y = 0;
			this.bossEvent = null;
		}

		public net.minecraft.client.gui.GuiGraphics getGuiGraphics() { return this.guiGraphics; }
		public int getX() { return this.x; }
		public int getY() { return this.y; }
		public net.minecraft.world.BossEvent getBossEvent() { return this.bossEvent; }
	}
}
