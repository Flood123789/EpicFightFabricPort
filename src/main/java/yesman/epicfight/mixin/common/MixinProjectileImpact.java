package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.event.entity.ProjectileImpactEvent;

@Mixin(Projectile.class)
public abstract class MixinProjectileImpact {
	@Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"), cancellable = true)
	private void epicfight$projectileImpactEvent(HitResult hitResult, CallbackInfo info) {
		ProjectileImpactEvent event = new ProjectileImpactEvent((Projectile)(Object)this, hitResult);
		boolean canceled = MinecraftForge.EVENT_BUS.post(event);

		if (canceled
			|| event.getImpactResult() == ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE
			|| (hitResult instanceof EntityHitResult && event.getImpactResult() == ProjectileImpactEvent.ImpactResult.SKIP_ENTITY)) {
			info.cancel();
		}
	}
}
