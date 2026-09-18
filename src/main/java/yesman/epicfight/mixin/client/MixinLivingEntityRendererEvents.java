package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.compat.PhysicsModCompat;
import yesman.epicfight.compat.WhackdollsCompat;
import yesman.epicfight.forgecompat.client.event.RenderLivingEvent;
import yesman.epicfight.forgecompat.common.MinecraftForge;

/** Supplies the Forge render events that Epic Fight's renderer expects on Fabric. */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererEvents<T extends LivingEntity, M extends EntityModel<T>> {
	@Inject(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At("HEAD"),
		cancellable = true
	)
	@SuppressWarnings("unchecked")
	private void epicfight$postFabricRenderPre(
		T entity, float entityYaw, float partialTick, PoseStack poseStack,
		MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo
	) {
		// Physics Mod recursively invokes the vanilla renderer while constructing a
		// corpse. Canceling that capture render leaves it with an empty ragdoll.
		if (PhysicsModCompat.isCapturingDeathModel() || WhackdollsCompat.isCapturingPlayerLayers()) {
			return;
		}

		LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>)(Object)this;
		RenderLivingEvent.Pre<T, M> event = new RenderLivingEvent.Pre<>(entity, renderer, partialTick, poseStack, bufferSource, packedLight);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			callbackInfo.cancel();
		}
	}

	@Inject(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At("RETURN")
	)
	@SuppressWarnings("unchecked")
	private void epicfight$postFabricRenderPost(
		T entity, float entityYaw, float partialTick, PoseStack poseStack,
		MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo
	) {
		LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>)(Object)this;
		MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, renderer, partialTick, poseStack, bufferSource, packedLight));
	}
}
