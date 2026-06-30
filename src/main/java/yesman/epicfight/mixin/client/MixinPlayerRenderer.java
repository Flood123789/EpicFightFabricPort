package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = PlayerRenderer.class, priority = 1200)
public abstract class MixinPlayerRenderer {
	@Inject(method = "setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", at = @At("TAIL"))
	private void epicfight$restoreVanillaArmPoses(AbstractClientPlayer player, CallbackInfo callbackInfo) {
		if (!(player instanceof LocalPlayer)) {
			return;
		}
		
		LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, LocalPlayerPatch.class);
		
		if (playerpatch == null || !playerpatch.isVanillaMode()) {
			return;
		}
		
		PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer)(Object)this).getModel();
		HumanoidModel.ArmPose mainArmPose = epicfight$getArmPose(player, InteractionHand.MAIN_HAND);
		HumanoidModel.ArmPose offArmPose = epicfight$getArmPose(player, InteractionHand.OFF_HAND);
		
		if (mainArmPose.isTwoHanded()) {
			offArmPose = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
		}
		
		if (player.getMainArm() == HumanoidArm.RIGHT) {
			model.rightArmPose = mainArmPose;
			model.leftArmPose = offArmPose;
		} else {
			model.rightArmPose = offArmPose;
			model.leftArmPose = mainArmPose;
		}
	}
	
	@Invoker("getArmPose")
	private static HumanoidModel.ArmPose epicfight$getArmPose(AbstractClientPlayer player, InteractionHand hand) {
		throw new AssertionError();
	}
}
