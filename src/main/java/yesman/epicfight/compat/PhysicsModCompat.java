package yesman.epicfight.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.main.EpicFightMod;

/** Small, optional bridge for Physics Mod Pro's death-model capture pass. */
public final class PhysicsModCompat {
	private static final Method GET_CURRENT_INSTANCE;
	private static final Field BLOCKIFY;

	static {
		Method getCurrentInstance = null;
		Field blockify = null;

		if (FabricLoader.getInstance().isModLoaded("physicsmod")) {
			try {
				Class<?> physicsModClass = Class.forName("net.diebuddies.physics.PhysicsMod");
				getCurrentInstance = physicsModClass.getMethod("getCurrentInstance");
				blockify = physicsModClass.getField("blockify");
			} catch (ReflectiveOperationException exception) {
				EpicFightMod.LOGGER.warn("Physics Mod is loaded, but its death-model capture API could not be bridged", exception);
			}
		}

		GET_CURRENT_INSTANCE = getCurrentInstance;
		BLOCKIFY = blockify;
	}

	private PhysicsModCompat() {
	}

	/**
	 * Physics Mod calls the vanilla living renderer recursively while this flag is
	 * active so it can turn the model's cubes into a ragdoll. Epic Fight must not
	 * cancel that one render call.
	 */
	public static boolean isCapturingDeathModel() {
		if (GET_CURRENT_INSTANCE == null || BLOCKIFY == null) {
			return false;
		}

		try {
			Object instance = GET_CURRENT_INSTANCE.invoke(null);
			return instance != null && BLOCKIFY.getBoolean(instance);
		} catch (ReflectiveOperationException | RuntimeException exception) {
			return false;
		}
	}
}
