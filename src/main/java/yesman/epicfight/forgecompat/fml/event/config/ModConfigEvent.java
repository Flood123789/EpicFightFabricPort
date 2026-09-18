package yesman.epicfight.forgecompat.fml.event.config;

import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.config.ModConfig;
import yesman.epicfight.forgecompat.fml.event.IModBusEvent;

public class ModConfigEvent extends Event implements IModBusEvent {
	private final ModConfig config;

	public ModConfigEvent(ModConfig config) {
		this.config = config;
	}

	public ModConfig getConfig() {
		return this.config;
	}

	public static class Loading extends ModConfigEvent {
		public Loading(ModConfig config) {
			super(config);
		}
	}

	public static class Reloading extends ModConfigEvent {
		public Reloading(ModConfig config) {
			super(config);
		}
	}
}
