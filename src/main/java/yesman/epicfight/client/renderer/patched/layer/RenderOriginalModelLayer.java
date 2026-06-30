package yesman.epicfight.client.renderer.patched.layer;

import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderOriginalModelLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>> extends PatchedLayer<E, T, M, RenderLayer<E, M>> {
	private static final Map<Armature, OpenMatrix4f[]> BIND_POSES = new WeakHashMap<>();
	private static final Vector3f ROTATION = new Vector3f();
	private static final Quaternionf QUATERNION = new Quaternionf();
	
	private final String parentJoint;
	private final Vec3f vec;
	private final Vec3f rot;
	
	public RenderOriginalModelLayer(String parentJoint, Vec3f vec, Vec3f rot) {
		this.parentJoint = parentJoint;
		this.vec = vec;
		this.rot = rot;
	}
	
	@Override
	protected void renderLayer(T entitypatch, E entityliving, RenderLayer<E, M> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		Armature armature = entitypatch.getArmature();
		OpenMatrix4f modelMatrix = poses[armature.searchJointByName(this.parentJoint).getId()];
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		poseStack.translate(this.vec.x, this.vec.y, this.vec.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(this.rot.y));
		poseStack.mulPose(Axis.XP.rotationDegrees(this.rot.x));
		poseStack.mulPose(Axis.ZP.rotationDegrees(this.rot.z));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		
		HumanoidPoseSnapshot poseSnapshot = this.applyHumanoidPose(vanillaLayer.getParentModel(), armature, poses);
		
		try {
			vanillaLayer.render(poseStack, buffer, packedLight, entityliving, entityliving.walkAnimation.position(), entityliving.walkAnimation.speed(), partialTicks, bob, yRot, xRot);
		} finally {
			if (poseSnapshot != null) {
				poseSnapshot.restore();
			}
		}
		
		poseStack.popPose();
	}
	
	private HumanoidPoseSnapshot applyHumanoidPose(EntityModel<E> model, Armature armature, OpenMatrix4f[] poses) {
		if (!(model instanceof HumanoidModel<?> humanoidModel) || !armature.hasJoint(this.parentJoint)) {
			return null;
		}
		
		HumanoidPoseSnapshot poseSnapshot = new HumanoidPoseSnapshot(humanoidModel);
		OpenMatrix4f[] bindPoses = BIND_POSES.computeIfAbsent(armature, currentArmature -> currentArmature.getPoseAsTransformMatrix(Pose.EMPTY_POSE, false));
		int rootId = armature.searchJointByName(this.parentJoint).getId();
		OpenMatrix4f inverseCurrentRoot = OpenMatrix4f.invert(poses[rootId], null);
		OpenMatrix4f inverseBindRoot = OpenMatrix4f.invert(bindPoses[rootId], null);
		
		if (inverseCurrentRoot == null || inverseBindRoot == null) {
			return null;
		}
		
		this.applyJointPose(humanoidModel.head, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Head");
		this.applyJointPose(humanoidModel.hat, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Head");
		this.applyJointPose(humanoidModel.body, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Chest");
		this.applyJointPose(humanoidModel.rightArm, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Arm_R");
		this.applyJointPose(humanoidModel.leftArm, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Arm_L");
		this.applyJointPose(humanoidModel.rightLeg, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Thigh_R");
		this.applyJointPose(humanoidModel.leftLeg, armature, poses, bindPoses, inverseCurrentRoot, inverseBindRoot, "Thigh_L");
		
		return poseSnapshot;
	}
	
	private void applyJointPose(ModelPart modelPart, Armature armature, OpenMatrix4f[] poses, OpenMatrix4f[] bindPoses, OpenMatrix4f inverseCurrentRoot, OpenMatrix4f inverseBindRoot, String jointName) {
		if (!armature.hasJoint(jointName)) {
			return;
		}
		
		int jointId = armature.searchJointByName(jointName).getId();
		OpenMatrix4f currentLocal = OpenMatrix4f.mul(inverseCurrentRoot, poses[jointId], null);
		OpenMatrix4f bindLocal = OpenMatrix4f.mul(inverseBindRoot, bindPoses[jointId], null);
		OpenMatrix4f inverseBindLocal = OpenMatrix4f.invert(bindLocal, null);
		
		if (inverseBindLocal == null) {
			return;
		}
		
		OpenMatrix4f animationDelta = OpenMatrix4f.mul(currentLocal, inverseBindLocal, null);
		animationDelta.toQuaternion(QUATERNION).getEulerAnglesXYZ(ROTATION);
		modelPart.xRot = ROTATION.x();
		modelPart.yRot = ROTATION.y();
		modelPart.zRot = ROTATION.z();
	}
	
	private static class HumanoidPoseSnapshot {
		private final ModelPart[] modelParts;
		private final PartPose[] poses;
		
		private HumanoidPoseSnapshot(HumanoidModel<?> model) {
			this.modelParts = new ModelPart[] {
				model.head,
				model.hat,
				model.body,
				model.rightArm,
				model.leftArm,
				model.rightLeg,
				model.leftLeg
			};
			this.poses = new PartPose[this.modelParts.length];
			
			for (int i = 0; i < this.modelParts.length; i++) {
				this.poses[i] = this.modelParts[i].storePose();
			}
		}
		
		private void restore() {
			for (int i = 0; i < this.modelParts.length; i++) {
				this.modelParts[i].loadPose(this.poses[i]);
			}
		}
	}
}
