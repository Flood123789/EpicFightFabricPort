package yesman.epicfight.compat.controlify;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import yesman.epicfight.main.EpicFightMod;

/**
 * Entry door to the Controlify integration for code that must not touch Controlify classes when the
 * mod is absent.
 *
 * <p>Controlify auto-generates a key-mapping binding for every modded key that has no binding
 * correlated with it, and it does so before it calls into mod entrypoints. Registering Epic Fight's
 * bindings from the client bootstrap (which runs earlier) keeps those duplicates out of the
 * controller settings; {@link EpicFightControlifyEntrypoint} still registers them later if this
 * runs too early to read the key mappings.</p>
 */
public final class ControlifyCompat {
	private static final String MOD_ID = "controlify";
	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded(MOD_ID);

	private ControlifyCompat() {
	}

	public static void registerBindings() {
		if (!LOADED) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft == null || minecraft.options == null) {
			// Binding registration reads the vanilla key mappings. The Controlify callbacks will
			// register them instead, which still happens before the bind registry is locked.
			return;
		}

		try {
			EpicFightControlifyEntrypoint.registerBindings();
		} catch (Throwable exception) {
			EpicFightMod.LOGGER.error("Failed to register the Epic Fight controller bindings with Controlify", exception);
		}
	}
}
