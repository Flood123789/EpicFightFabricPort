package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.monster.EnderMan;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.event.entity.EntityTeleportEvent;

@Mixin(EnderMan.class)
public abstract class MixinEnderMan {
	@Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
	private void epicfight$teleportEvent(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
		EnderMan self = (EnderMan)(Object)this;
		if (MinecraftForge.EVENT_BUS.post(new EntityTeleportEvent.EnderEntity(self, x, y, z))) {
			info.setReturnValue(false);
		}
	}
}
