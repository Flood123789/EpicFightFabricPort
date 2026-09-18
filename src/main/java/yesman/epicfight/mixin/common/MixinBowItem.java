package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.event.entity.player.ArrowLooseEvent;

@Mixin(BowItem.class)
public abstract class MixinBowItem {
	@Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", at = @At("HEAD"))
	private void epicfight$arrowLooseEvent(ItemStack bow, Level level, LivingEntity livingEntity, int timeLeft, CallbackInfo info) {
		if (livingEntity instanceof Player player) {
			int charge = bow.getUseDuration() - timeLeft;
			boolean hasAmmo = player.getAbilities().instabuild || !player.getProjectile(bow).isEmpty();
			MinecraftForge.EVENT_BUS.post(new ArrowLooseEvent(player, bow, level, charge, hasAmmo));
		}
	}
}
