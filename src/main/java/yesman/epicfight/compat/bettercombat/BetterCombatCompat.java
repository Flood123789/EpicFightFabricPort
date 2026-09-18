package yesman.epicfight.compat.bettercombat;

import java.util.UUID;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.bettercombat.logic.PlayerAttackProperties;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Hands combat ownership to Better Combat in vanilla mode and back to Epic Fight in Epic Fight mode.
 * Better Combat is an optional dependency; callers must only enter the implementation when it is loaded.
 */
public final class BetterCombatCompat {
	private static final String MOD_ID = "bettercombat";
	private static final UUID DUAL_WIELDING_SPEED_MODIFIER_ID = UUID.fromString("6b364332-0dc4-11ed-861d-0242ac120002");
	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded(MOD_ID);

	private BetterCombatCompat() {
	}

	public static boolean isLoaded() {
		return LOADED;
	}

	public static boolean isEpicFightMode(Player player) {
		if (player == null) {
			return false;
		}

		PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		return playerPatch != null && playerPatch.isEpicFightMode();
	}

	public static void onPlayerModeChanged(Player player, boolean epicFightMode) {
		if (!LOADED || player == null) {
			return;
		}

		Common.setMode(player, epicFightMode);
	}

	private static final class Common {
		private static void setMode(Player player, boolean epicFightMode) {
			PlayerAttackProperties attackProperties = (PlayerAttackProperties)(Object)player;
			int comboCount = attackProperties.getComboCount();

			if (epicFightMode) {
				attackProperties.setComboCount(-1);
				var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);

				if (attackSpeed != null) {
					attackSpeed.removeModifier(DUAL_WIELDING_SPEED_MODIFIER_ID);
				}
			} else if (comboCount < 0) {
				attackProperties.setComboCount(0);
			}
		}
	}

	static {
		if (LOADED) {
			EpicFightMod.LOGGER.info("Better Combat compatibility enabled: vanilla mode uses Better Combat; Epic Fight mode suspends it");
		}
	}
}
