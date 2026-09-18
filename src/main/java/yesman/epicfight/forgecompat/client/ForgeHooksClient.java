package yesman.epicfight.forgecompat.client;

import java.util.Comparator;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.forgecompat.client.event.InputEvent;

public class ForgeHooksClient {
	public static String getArmorTexture(Entity entity, ItemStack stack, String defaultTexture, EquipmentSlot slot, String type) {
		return defaultTexture;
	}

	public static Model getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, Model defaultModel) {
		return defaultModel;
	}

	public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel bakedModel, ItemDisplayContext transformType, boolean leftHand) {
		bakedModel.getTransforms().getTransform(transformType).apply(leftHand, poseStack);
		return bakedModel;
	}

	public static List<BakedModel> getRenderPasses(BakedModel bakedModel, ItemStack stack, boolean fabulous) {
		return java.util.Collections.singletonList(bakedModel);
	}

	public static InputEvent.InteractionKeyMappingTriggered onClickInput(int mouseButton, KeyMapping key, InteractionHand hand) {
		return new InputEvent.InteractionKeyMappingTriggered(mouseButton, key, hand);
	}

	public static float getGuiFarPlane() {
		return 21000.0F;
	}

	public static Comparator<ParticleRenderType> makeParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
		return (o1, o2) -> Integer.compare(renderOrder.indexOf(o1), renderOrder.indexOf(o2));
	}

	public static boolean shouldRiderSit(Entity vehicle) {
		return vehicle != null;
	}
}
