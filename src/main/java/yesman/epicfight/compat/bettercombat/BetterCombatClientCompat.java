package yesman.epicfight.compat.bettercombat;

import net.bettercombat.api.MinecraftClient_BetterCombat;
import net.bettercombat.client.BetterCombatClient;
import net.minecraft.client.Minecraft;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public final class BetterCombatClientCompat {
	private BetterCombatClientCompat() {
	}

	public static void onLocalPlayerModeChanged(boolean epicFightMode) {
		if (BetterCombatCompat.isLoaded()) {
			Loaded.setMode(Minecraft.getInstance(), epicFightMode);
		}
	}

	public static void synchronizeMode(Minecraft minecraft) {
		if (!BetterCombatCompat.isLoaded()) {
			return;
		}

		if (minecraft.player == null || minecraft.getConnection() == null) {
			Loaded.resetConnection();
			return;
		}

		LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(minecraft.player, LocalPlayerPatch.class);
		boolean epicFightMode = playerPatch != null && playerPatch.isEpicFightMode();
		BetterCombatCompat.onPlayerModeChanged(minecraft.player, epicFightMode);
		Loaded.setMode(minecraft, epicFightMode);
	}

	public static String diagnosticState() {
		return BetterCombatCompat.isLoaded() ? Loaded.diagnosticState() : "not-loaded";
	}

	/** Kept separate so the optional Better Combat classes are never resolved when the mod is absent. */
	private static final class Loaded {
		private static boolean suspended;
		private static boolean enabledBeforeSuspend;

		private static void setMode(Minecraft minecraft, boolean epicFightMode) {
			if (epicFightMode) {
				if (!suspended) {
					enabledBeforeSuspend = BetterCombatClient.ENABLED;
					suspended = true;
				} else if (BetterCombatClient.ENABLED) {
					// Better Combat's server config can arrive after the Epic Fight mode packet.
					enabledBeforeSuspend = true;
				}

				if (BetterCombatClient.ENABLED) {
					((MinecraftClient_BetterCombat)(Object)minecraft).cancelUpswing();
					BetterCombatClient.ENABLED = false;
				}
			} else if (suspended) {
				BetterCombatClient.ENABLED = enabledBeforeSuspend;
				suspended = false;
				enabledBeforeSuspend = false;
			}
		}

		private static void resetConnection() {
			if (suspended) {
				// Leaving a world while Epic Fight owns combat must not strand Better
				// Combat in its suspended state for the next world in the same client.
				BetterCombatClient.ENABLED = enabledBeforeSuspend;
			}

			suspended = false;
			enabledBeforeSuspend = false;
		}

		private static String diagnosticState() {
			return "enabled=" + BetterCombatClient.ENABLED + ",suspended=" + suspended + ",restore=" + enabledBeforeSuspend;
		}
	}
}
