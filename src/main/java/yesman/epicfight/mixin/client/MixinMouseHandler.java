package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.MouseHandler;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = MouseHandler.class, priority = 1100)
public abstract class MixinMouseHandler {
	@ModifyArgs(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
		),
		method = "turnPlayer()V",
		require = 0
	)
	private void epicfight$turnPlayer(Args args) {
		double yRot = args.get(0);
		double xRot = args.get(1);
		
		if (EpicFightCameraAPI.getInstance().turnCamera(yRot, xRot)) {
			args.set(0, 0.0D);
			args.set(1, 0.0D);
		}
	}
}
