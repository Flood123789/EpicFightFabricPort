package yesman.epicfight.mixin.bettercombat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.bettercombat.logic.PlayerAttackHelper;
import yesman.epicfight.compat.bettercombat.BetterCombatCompat;

@Mixin(value = PlayerAttackHelper.class, remap = false)
public abstract class MixinPlayerAttackHelper {
	@Inject(method = "getCurrentAttack", at = @At("HEAD"), cancellable = true, remap = false)
	private static void epicfight$disableBetterCombatAttack(Player player, int comboCount, CallbackInfoReturnable<Object> callbackInfo) {
		if (BetterCombatCompat.isEpicFightMode(player)) {
			callbackInfo.setReturnValue(null);
		}
	}

	@Inject(method = {"isDualWielding", "isTwoHandedWielding"}, at = @At("HEAD"), cancellable = true, remap = false)
	private static void epicfight$disableBetterCombatWielding(Player player, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (BetterCombatCompat.isEpicFightMode(player)) {
			callbackInfo.setReturnValue(false);
		}
	}
}
