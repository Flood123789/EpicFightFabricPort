package yesman.epicfight.forgecompat.client.event;

import java.util.Map;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ScreenEvent extends Event {
	private final Screen screen;

	public ScreenEvent(Screen screen) {
		this.screen = screen;
	}

	public Screen getScreen() {
		return this.screen;
	}

	public static class Init extends ScreenEvent {
		public Init(Screen screen) {
			super(screen);
		}

		public static class Post extends Init {
			public Post(Screen screen) {
				super(screen);
			}

			public void addListener(GuiEventListener listener) {
				// Added in GUI init post
			}
		}
	}

	public static class Render extends ScreenEvent {
		public Render(Screen screen) {
			super(screen);
		}

		public static class Post extends Render {
			public Post(Screen screen) {
				super(screen);
			}
		}
	}

	@Cancelable
	public static class KeyPressed extends ScreenEvent {
		private final int keyCode;
		private final int scanCode;
		private final int modifiers;

		public KeyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
			super(screen);
			this.keyCode = keyCode;
			this.scanCode = scanCode;
			this.modifiers = modifiers;
		}

		public int getKeyCode() {
			return this.keyCode;
		}

		public int getScanCode() {
			return this.scanCode;
		}

		public int getModifiers() {
			return this.modifiers;
		}

		@Cancelable
		public static class Pre extends KeyPressed {
			public Pre(Screen screen, int keyCode, int scanCode, int modifiers) {
				super(screen, keyCode, scanCode, modifiers);
			}
		}
	}

	@Cancelable
	public static class MouseButtonPressed extends ScreenEvent {
		private final double mouseX;
		private final double mouseY;
		private final int button;

		public MouseButtonPressed(Screen screen, double mouseX, double mouseY, int button) {
			super(screen);
			this.mouseX = mouseX;
			this.mouseY = mouseY;
			this.button = button;
		}

		public double getMouseX() {
			return this.mouseX;
		}

		public double getMouseY() {
			return this.mouseY;
		}

		public int getButton() {
			return this.button;
		}

		@Cancelable
		public static class Pre extends MouseButtonPressed {
			public Pre(Screen screen, double mouseX, double mouseY, int button) {
				super(screen, mouseX, mouseY, button);
			}
		}
	}

	@Cancelable
	public static class MouseButtonReleased extends ScreenEvent {
		private final double mouseX;
		private final double mouseY;
		private final int button;

		public MouseButtonReleased(Screen screen, double mouseX, double mouseY, int button) {
			super(screen);
			this.mouseX = mouseX;
			this.mouseY = mouseY;
			this.button = button;
		}

		public double getMouseX() {
			return this.mouseX;
		}

		public double getMouseY() {
			return this.mouseY;
		}

		public int getButton() {
			return this.button;
		}

		@Cancelable
		public static class Pre extends MouseButtonReleased {
			public Pre(Screen screen, double mouseX, double mouseY, int button) {
				super(screen, mouseX, mouseY, button);
			}
		}
	}
}
