package yesman.epicfight.compat;

import java.lang.reflect.Method;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.main.EpicFightMod;

/** Optional bridge that permits Whackdolls' recursive real-player layer pass. */
public final class WhackdollsCompat {
	private static final Method IS_CAPTURING_PLAYER_LAYERS;
	private static final Method IS_CAPTURING_PHYSICS_ENTITY;
	private static final Method GET_CLIENT_RAGDOLL;
	private static final Method HAS_CLIENT_MOB_CORPSE;

	static {
		Method captureCheck = null;
		Method getClientRagdoll = null;
		Method hasClientMobCorpse = null;
		Method physicsCaptureCheck = null;
		if (FabricLoader.getInstance().isModLoaded("whackdolls")) {
			try {
				Class<?> context = Class.forName("whackdolls.flood.client.render.RagdollRenderContext");
				captureCheck = context.getMethod("isCapturingPlayerLayers");
				Class<?> physicsContext = Class.forName("whackdolls.flood.client.render.PhysicsCaptureContext");
				physicsCaptureCheck = physicsContext.getMethod("isCapturingEntity", LivingEntity.class);
				Class<?> ragdollEntity = Class.forName("whackdolls.flood.entity.RagdollEntity");
				getClientRagdoll = ragdollEntity.getMethod("getClientRagdoll", java.util.UUID.class);
				Class<?> mobRagdollEntity = Class.forName("whackdolls.flood.entity.MobRagdollEntity");
				hasClientMobCorpse = mobRagdollEntity.getMethod("hasClientCorpse", java.util.UUID.class);
			} catch (ReflectiveOperationException exception) {
				EpicFightMod.LOGGER.warn("Whackdolls is loaded, but its renderer compatibility bridge is unavailable", exception);
			}
		}
		IS_CAPTURING_PLAYER_LAYERS = captureCheck;
		IS_CAPTURING_PHYSICS_ENTITY = physicsCaptureCheck;
		GET_CLIENT_RAGDOLL = getClientRagdoll;
		HAS_CLIENT_MOB_CORPSE = hasClientMobCorpse;
	}

	private WhackdollsCompat() {
	}

	public static boolean isCapturingPlayerLayers() {
		if (IS_CAPTURING_PLAYER_LAYERS == null) {
			return false;
		}
		try {
			return Boolean.TRUE.equals(IS_CAPTURING_PLAYER_LAYERS.invoke(null));
		} catch (ReflectiveOperationException | RuntimeException exception) {
			return false;
		}
	}

	public static boolean shouldUseVanillaRenderer(LivingEntity entity) {
		if (IS_CAPTURING_PHYSICS_ENTITY != null) {
			try {
				if (Boolean.TRUE.equals(IS_CAPTURING_PHYSICS_ENTITY.invoke(null, entity))) {
					return true;
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
				// Continue with the live-player compatibility path.
			}
		}
		if (!(entity instanceof Player) || GET_CLIENT_RAGDOLL == null) {
			return false;
		}
		try {
			return GET_CLIENT_RAGDOLL.invoke(null, entity.getUUID()) != null;
		} catch (ReflectiveOperationException | RuntimeException exception) {
			return false;
		}
	}

	/**
	 * Cancels Epic Fight's armature renderer for the original dead mob after
	 * Whackdolls has created the replacement corpse. This also covers render
	 * calls made outside the normal entity dispatcher by compatibility mods.
	 */
	public static boolean shouldSuppressDeadMobRenderer(LivingEntity entity) {
		if (entity instanceof Player || !entity.isDeadOrDying() || HAS_CLIENT_MOB_CORPSE == null) {
			return false;
		}
		try {
			return Boolean.TRUE.equals(HAS_CLIENT_MOB_CORPSE.invoke(null, entity.getUUID()));
		} catch (ReflectiveOperationException | RuntimeException exception) {
			return false;
		}
	}
}
