package yesman.epicfight.forgecompat.client.event;

import net.minecraft.client.KeyMapping;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class InputEvent extends Event {
	@Cancelable
	public static class InteractionKeyMappingTriggered extends InputEvent {
		private final int button;
		private final KeyMapping keyMapping;
		private final net.minecraft.world.InteractionHand hand;
		private boolean swingHand = true;

		public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, net.minecraft.world.InteractionHand hand) {
			this.button = button;
			this.keyMapping = keyMapping;
			this.hand = hand;
		}

		public boolean isAttack() {
			return this.button == 0;
		}

		public boolean isUseItem() {
			return this.button == 1;
		}

		public KeyMapping getKeyMapping() {
			return this.keyMapping;
		}

		public net.minecraft.world.InteractionHand getHand() {
			return this.hand;
		}

		public boolean shouldSwingHand() {
			return this.swingHand;
		}

		public void setSwingHand(boolean swingHand) {
			this.swingHand = swingHand;
		}
	}

	@Cancelable
	public static class Key extends InputEvent {
		private final int key;
		private final int scanCode;
		private final int action;
		private final int modifiers;

		public Key(int key, int scanCode, int action, int modifiers) {
			this.key = key;
			this.scanCode = scanCode;
			this.action = action;
			this.modifiers = modifiers;
		}

		public int getKey() {
			return this.key;
		}

		public int getScanCode() {
			return this.scanCode;
		}

		public int getAction() {
			return this.action;
		}

		public int getModifiers() {
			return this.modifiers;
		}
	}

	@Cancelable
	public static class MouseScrollingEvent extends InputEvent {
		private final double scrollDeltaX;
		private final double scrollDeltaY;

		public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY) {
			this.scrollDeltaX = scrollDeltaX;
			this.scrollDeltaY = scrollDeltaY;
		}

		public double getScrollDeltaX() {
			return this.scrollDeltaX;
		}

		public double getScrollDeltaY() {
			return this.scrollDeltaY;
		}
	}
}
