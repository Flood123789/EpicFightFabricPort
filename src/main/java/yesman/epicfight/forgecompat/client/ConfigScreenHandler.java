package yesman.epicfight.forgecompat.client;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import yesman.epicfight.forgecompat.fml.IExtensionPoint;

public class ConfigScreenHandler {
	public static class ConfigScreenFactory implements IExtensionPoint<ConfigScreenFactory> {
		private final BiFunction<Minecraft, Screen, Screen> screenFunction;

		public ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) {
			this.screenFunction = screenFunction;
		}

		public ConfigScreenFactory(Function<Screen, Screen> screenFunction) {
			this((mc, screen) -> screenFunction.apply(screen));
		}

		public BiFunction<Minecraft, Screen, Screen> screenFunction() {
			return this.screenFunction;
		}
	}
}
