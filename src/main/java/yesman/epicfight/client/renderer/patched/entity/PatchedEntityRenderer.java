package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.forgecompat.client.event.RenderNameTagEvent;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.eventbus.api.Event.Result;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.mixin.client.MixinEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, R extends EntityRenderer<E>, AM extends SkinnedMesh> {
	public void render(E entity, T entitypatch, R renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(entity, entity.getDisplayName(), renderer, poseStack, buffer, packedLight, partialTicks);
		MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
		
		MixinEntityRenderer entityRendererAccessor = (MixinEntityRenderer)renderer;
		
		if (renderNameplateEvent.getResult() != Result.DENY && (renderNameplateEvent.getResult() == Result.ALLOW || this.shouldShowName(entityRendererAccessor, entity))) {
			entityRendererAccessor.invokeRenderNameTag(entity, renderNameplateEvent.getContent(), poseStack, buffer, packedLight);
		}
	}

	private boolean shouldShowName(MixinEntityRenderer entityRendererAccessor, E entity) {
		try {
			return entityRendererAccessor.invokeShouldShowName(entity);
		} catch (NullPointerException exception) {
			// Vivecraft can enter this path while Physics Mod renders a death model without an active dispatcher camera.
			return false;
		}
	}
	
	public void mulPoseStack(PoseStack poseStack, Armature armature, E entity, T entitypatch, float partialTicks) {
		OpenMatrix4f modelMatrix = entitypatch.getModelMatrix(partialTicks);
        poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F));
        MathUtils.mulStack(poseStack, modelMatrix);
        
        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
        	poseStack.translate(0.0D, entity.getBbHeight() + 0.1F, 0.0D);
        	poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(180.0F));
		}
	}
	
	public void setArmaturePose(T entitypatch, Armature armature, float partialTicks) {
		Pose pose = entitypatch.getAnimator().getPose(partialTicks);
        this.setJointTransforms(entitypatch, armature, pose, partialTicks);
        armature.setPose(pose);
	}
	
	public AssetAccessor<AM> getMeshProvider(T entitypatch) {
		return this.getDefaultMesh();
	}
	
	public abstract AssetAccessor<AM> getDefaultMesh();
	
	/**
	 * Developers shouldn't implement any interpolations in this method
	 * Use {@link LivingEntityPatch#poseTick} instead
	 */
	public void setJointTransforms(T entitypatch, Armature armature, Pose pose, float partialTicks) {
	}
}
